package com.cnunez.docufast.admin.user.create.model

import android.util.Log
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class CreateUserModel(
    private val mainAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) : CreateUserContract.Model {

    companion object {
        private const val TAG = "CreateUserModel"
    }

    override fun createUser(
        newUserFromPresenter: User,
        passwordForNewUser: String,
        adminPasswordToCheck: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val currentAdminUser = mainAuth.currentUser
        if (currentAdminUser == null || currentAdminUser.email.isNullOrEmpty()) {
            callback(false, "Admin no autenticado o email de admin no disponible.")
            return
        }
        val adminEmail = currentAdminUser.email!!

        val credential = EmailAuthProvider.getCredential(adminEmail, adminPasswordToCheck)
        currentAdminUser.reauthenticate(credential)
            .addOnSuccessListener {
                mainAuth.createUserWithEmailAndPassword(newUserFromPresenter.email, passwordForNewUser)
                    .addOnSuccessListener { authResult ->
                        val newFirebaseUser = authResult.user
                        if (newFirebaseUser == null) {
                            callback(false, "Error creando usuario: FirebaseUser nulo.")
                            reLoginAdmin(mainAuth, adminEmail, adminPasswordToCheck, null)
                            return@addOnSuccessListener
                        }
                        saveUserToRealtimeDB(newFirebaseUser, newUserFromPresenter, callback, mainAuth, adminEmail, adminPasswordToCheck)
                    }
                    .addOnFailureListener { createUserException ->
                        val errorMessage = if (createUserException is FirebaseAuthUserCollisionException) {
                            "El email ya está en uso."
                        } else {
                            "Error creación Auth: ${createUserException.localizedMessage}"
                        }
                        callback(false, errorMessage)
                        reLoginAdmin(mainAuth, adminEmail, adminPasswordToCheck, null)
                    }
            }
            .addOnFailureListener { reauthException ->
                callback(false, "Contraseña de admin incorrecta o error de reautenticación: ${reauthException.localizedMessage}")
            }
    }

    private fun saveUserToRealtimeDB(
        newlyCreatedAuthUser: FirebaseUser,
        originalUserData: User,
        callback: (Boolean, String?) -> Unit,
        authToReLogin: FirebaseAuth,
        adminEmailToReLogin: String,
        adminPasswordToReLogin: String
    ) {
        val completeNewUser = originalUserData.copy(
            id = newlyCreatedAuthUser.uid,
            // Asegúrate que los campos de 'User' se inicializan correctamente aquí o en el Presenter
            // Por ejemplo, si 'organization', 'stability', etc., vienen del presenter, ya están en originalUserData.
            // Si tienen valores por defecto que se establecen aquí:
            organization = originalUserData.organization.takeIf { it.isNotEmpty() } ?: "", // Ejemplo
            stability = originalUserData.stability ?: 0, // Ejemplo
            createdAt = originalUserData.createdAt ?: System.currentTimeMillis(), // Ejemplo
            isSelected = originalUserData.isSelected ?: false // Ejemplo
        )

        val userPath = "users/${completeNewUser.id}"
        val updates = hashMapOf<String, Any?>()
        updates[userPath] = completeNewUser // Guarda el objeto User completo

        if (completeNewUser.role == "ADMIN") {
            updates["admins/${completeNewUser.id}"] = true
        }

        db.reference.updateChildren(updates)
            .addOnSuccessListener {
                reLoginAdmin(authToReLogin, adminEmailToReLogin, adminPasswordToReLogin) { success, error ->
                    if (success) {
                        callback(true, null)
                    } else {
                        Log.w(TAG, "Usuario creado y guardado en DB, pero falló el re-login del admin: $error")
                        callback(true, "Usuario creado, pero hubo un problema al refrescar la sesión del admin.")
                    }
                }
            }
            .addOnFailureListener { dbException ->
                Log.e(TAG, "Error guardando usuario en RTDB: ${dbException.message}", dbException)
                // Considerar eliminar el usuario de Auth si falla la escritura en DB para mantener consistencia
                newlyCreatedAuthUser.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        Log.d(TAG, "Usuario de Auth eliminado debido a fallo en RTDB.")
                    } else {
                        Log.w(TAG, "No se pudo eliminar el usuario de Auth tras fallo en RTDB.")
                    }
                }
                callback(false, "Error guardando datos en DB: ${dbException.localizedMessage}")
                reLoginAdmin(authToReLogin, adminEmailToReLogin, adminPasswordToReLogin, null)
            }
    }

    private fun reLoginAdmin(
        auth: FirebaseAuth,
        adminEmail: String,
        adminPass: String,
        onComplete: ((Boolean, String?) -> Unit)? = null // Callback opcional
    ) {
        // Solo re-loguear si el usuario actual no es ya el admin
        // (createUserWithEmailAndPassword cambia el usuario actual al nuevo usuario)
        if (auth.currentUser == null || auth.currentUser?.email != adminEmail) {
            Log.d(TAG, "Re-logueando al admin: $adminEmail")
            auth.signInWithEmailAndPassword(adminEmail, adminPass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Admin re-logueado exitosamente.")
                        onComplete?.invoke(true, null)
                    } else {
                        Log.e(TAG, "Fallo al re-loguear al admin: ${task.exception?.message}")
                        onComplete?.invoke(false, "Fallo al re-loguear al admin: ${task.exception?.localizedMessage}")
                    }
                }
        } else {
            Log.d(TAG, "Admin ya está logueado. No se necesita re-login.")
            onComplete?.invoke(true, null) // Ya es el admin
        }
    }
}