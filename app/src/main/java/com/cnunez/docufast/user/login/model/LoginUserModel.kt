package com.cnunez.docufast.user.login.model

import com.cnunez.docufast.user.login.contract.LoginUserContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.ktx.Firebase

class LoginUserModel : LoginUserContract.Model {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = Firebase.database

    override fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "Usuario no registrado"
                        is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas"
                        else -> "Error de autenticación: ${task.exception?.message}"
                    }
                    callback(false, errorMessage)
                }
            }
    }

    override fun getUserData(uid: String, callback: (User?, String?) -> Unit) {
        // 1) intenta /users/{uid}
        database.getReference("users").child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)?.apply {
                        id = snapshot.key ?: ""
                        // Normaliza rol
                        role = role.ifBlank { "USER" }.uppercase()
                    }
                    callback(user, null)
                } else {
                    // 2) fallback: /users orderByChild("email") == currentUser.email
                    val email = auth.currentUser?.email
                    if (email.isNullOrBlank()) {
                        callback(null, "Datos de usuario no encontrados (sin email en sesión)")
                        return@addOnSuccessListener
                    }
                    database.getReference("users")
                        .orderByChild("email")
                        .equalTo(email)
                        .limitToFirst(1)
                        .get()
                        .addOnSuccessListener { q ->
                            val child = q.children.firstOrNull()
                            val user = child?.getValue(User::class.java)?.apply {
                                id = child.key ?: ""
                                role = role.ifBlank { "USER" }.uppercase()
                            }
                            if (user == null) {
                                callback(null, "Datos de usuario no encontrados en RTDB")
                            } else {
                                callback(user, null)
                            }
                        }
                        .addOnFailureListener { e ->
                            callback(null, "Error al leer datos por email: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(null, "Error al leer datos: ${e.message}")
            }
    }
}
