package com.cnunez.docufast.admin.registerNewAdmin.model

import android.util.Log
import com.cnunez.docufast.admin.registerNewAdmin.contract.RegisterAdminContract
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterAdminModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : RegisterAdminContract.Model {

    override fun registerAdmin(
        fullName: String,
        email: String,
        password: String,
        orgId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        Log.d("RegisterAdmin", "Iniciando registro de admin")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d("RegisterAdmin", "Usuario auth creado. Autenticando...")

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val uid = auth.currentUser!!.uid
                        Log.d("RegisterAdmin", "Usuario autenticado con UID: $uid")

                        val newUser = User(
                            id = uid,
                            name = fullName,
                            email = email,
                            organization = orgId,
                            workGroups = emptyMap(),
                            role = "ADMIN",
                            stability = 1,
                            createdAt = System.currentTimeMillis(),
                            isSelected = false
                        )

                        Log.d("RegisterAdmin", "Guardando usuario en /users/$uid")
                        database.reference.child("users").child(uid)
                            .setValue(newUser.toMap())
                            .addOnSuccessListener {
                                Log.d("RegisterAdmin", "Registro de administrador completado exitosamente.")
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterAdmin", "Error guardando usuario en DB: ${e.message}")
                                callback(false, "Error al guardar usuario en la base de datos: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("RegisterAdmin", "Error autenticando nuevo usuario: ${e.message}")
                        callback(false, "Error al autenticar nuevo usuario: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("RegisterAdmin", "Error creando usuario Auth: ${e.message}")
                callback(false, "Error al crear usuario en Auth: ${e.message}")
            }
    }

    override fun createUser(
        fullName: String,
        email: String,
        password: String,
        orgId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            Log.w("CreateUser", "Usuario no autenticado")
            callback(false, "Debes iniciar sesión como administrador.")
            return
        }

        database.reference.child("users").child(currentUid).child("role")
            .get()
            .addOnSuccessListener { snap ->
                val roleActual = snap.getValue(String::class.java)
                Log.d("CreateUser", "Rol del UID $currentUid: $roleActual")
                if (roleActual != "ADMIN") {
                    callback(false, "Solo los administradores pueden crear usuarios.")
                    return@addOnSuccessListener
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authRes ->
                        val newUid = authRes.user!!.uid
                        val newUser = User(
                            id = newUid,
                            name = fullName,
                            email = email,
                            organization = orgId,
                            workGroups = mapOf(),
                            role = "ADMIN",
                            stability = 1,
                            createdAt = System.currentTimeMillis(),
                            isSelected = false
                        )

                        database.reference.child("users").child(newUid)
                            .setValue(newUser.toMap())
                            .addOnSuccessListener {
                                Log.d("CreateUser", "Usuario USER creado exitosamente.")
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.e("CreateUser", "Error guardando USER en DB: ${e.message}")
                                callback(false, "Error al guardar usuario en la base de datos: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CreateUser", "Error creando cuenta USER: ${e.message}")
                        callback(false, "Error al crear cuenta de usuario: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CreateUser", "Error obteniendo rol actual: ${e.message}")
                callback(false, "Error al verificar el rol del administrador: ${e.message}")
            }
    }
}
