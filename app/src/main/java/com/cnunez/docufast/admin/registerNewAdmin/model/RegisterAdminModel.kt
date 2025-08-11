package com.cnunez.docufast.admin.registerNewAdmin.model

import com.cnunez.docufast.admin.registerNewAdmin.contract.RegisterAdminContract
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterAdminModel : RegisterAdminContract.Model {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun registerAdmin(
        fullName: String,
        email: String,
        password: String,
        organization: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener callback(false, "UID vacío")
                val now = System.currentTimeMillis()

                val adminUser = User(
                    id = userId,
                    name = fullName,
                    email = email,
                    organization = organization,
                    workGroups = emptyMap(), // Importante: Map<String, Boolean>
                    role = "ADMIN",
                    stability = 0,
                    createdAt = now,
                    isSelected = false
                )

                db.child("users").child(userId).setValue(adminUser.toMap())
                    .addOnSuccessListener { callback(true, null) }
                    .addOnFailureListener { e ->
                        callback(false, "Error en base de datos: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Error de autenticación: ${e.message}")
            }
    }


    override fun createUser(
        fullName: String,
        email: String,
        password: String,
        organization: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener callback(false, "UID vacío")
                val now = System.currentTimeMillis()

                val normalUser = User(
                    id = userId,
                    name = fullName,
                    email = email,
                    organization = organization,
                    workGroups = emptyMap(),
                    role = "USER",
                    stability = 0,
                    createdAt = now,
                    isSelected = false
                )

                FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(userId)
                    .setValue(normalUser.toMap())
                    .addOnSuccessListener { callback(true, null) }
                    .addOnFailureListener { e ->
                        callback(false, "Error en base de datos: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Error de autenticación: ${e.message}")
            }
    }
}