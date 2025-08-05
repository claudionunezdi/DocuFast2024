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
                val userId = authResult.user?.uid ?: ""
                val adminUser = User(
                    id = userId,
                    name = fullName,
                    email = email,
                    organization = organization,
                    role = "ADMIN"
                )

                db.child("users").child(userId).setValue(adminUser)
                    .addOnSuccessListener {
                        callback(true, null)
                    }
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
                val userId = authResult.user?.uid ?: ""
                val normalUser = User(
                    id = userId,
                    name = fullName,
                    email = email,
                    organization = organization,
                    role = "USER"
                )

                db.child("users").child(userId).setValue(normalUser)
                    .addOnSuccessListener {
                        callback(true, null)
                    }
                    .addOnFailureListener { e ->
                        callback(false, "Error en base de datos: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Error de autenticación: ${e.message}")
            }
    }
}