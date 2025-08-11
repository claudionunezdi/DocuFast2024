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
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val currentAdminUser = mainAuth.currentUser
        if (currentAdminUser == null || currentAdminUser.email.isNullOrEmpty()) {
            callback(false, "Admin no autenticado o email de admin no disponible.", null)
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
                            callback(false, "Error creando usuario: FirebaseUser nulo.", null)
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
                        callback(false, errorMessage, null)
                        reLoginAdmin(mainAuth, adminEmail, adminPasswordToCheck, null)
                    }
            }
            .addOnFailureListener { reauthException ->
                callback(false, "Contraseña de admin incorrecta o error de reautenticación: ${reauthException.localizedMessage}", null)
            }
    }

    private fun saveUserToRealtimeDB(
        newlyCreatedAuthUser: FirebaseUser,
        originalUserData: User,
        callback: (Boolean, String?, String?) -> Unit,
        authToReLogin: FirebaseAuth,
        adminEmailToReLogin: String,
        adminPasswordToReLogin: String
    ) {
        val completeNewUser = originalUserData.copy(
            id = newlyCreatedAuthUser.uid,
            organization = originalUserData.organization.takeIf { it.isNotEmpty() } ?: "",
            stability = originalUserData.stability ?: 0,
            createdAt = originalUserData.createdAt ?: System.currentTimeMillis(),
            isSelected = originalUserData.isSelected ?: false
        )

        val userPath = "users/${completeNewUser.id}"
        val updates = hashMapOf<String, Any?>(
            userPath to completeNewUser
        )

        if (completeNewUser.role == "ADMIN") {
            updates["admins/${completeNewUser.id}"] = true
        }

        db.reference.updateChildren(updates)
            .addOnSuccessListener {
                reLoginAdmin(authToReLogin, adminEmailToReLogin, adminPasswordToReLogin) { success, error ->
                    if (success) {
                        // DEVOLVEMOS EL UID CREADO
                        callback(true, null, completeNewUser.id)
                    } else {
                        Log.w(TAG, "Usuario creado y guardado en DB, pero falló el re-login del admin: $error")
                        callback(true, "Usuario creado, pero hubo un problema al refrescar la sesión del admin.", completeNewUser.id)
                    }
                }
            }
            .addOnFailureListener { dbException ->
                Log.e(TAG, "Error guardando usuario en RTDB: ${dbException.message}", dbException)
                newlyCreatedAuthUser.delete().addOnCompleteListener { /* best-effort */ }
                callback(false, "Error guardando datos en DB: ${dbException.localizedMessage}", null)
                reLoginAdmin(authToReLogin, adminEmailToReLogin, adminPasswordToReLogin, null)
            }
    }

    private fun reLoginAdmin(
        auth: FirebaseAuth,
        adminEmail: String,
        adminPass: String,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        if (auth.currentUser == null || auth.currentUser?.email != adminEmail) {
            auth.signInWithEmailAndPassword(adminEmail, adminPass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) onComplete?.invoke(true, null)
                    else onComplete?.invoke(false, task.exception?.localizedMessage)
                }
        } else {
            onComplete?.invoke(true, null)
        }
    }
}
