package com.cnunez.docufast.user.login.Model

import com.cnunez.docufast.user.login.Contract.LoginUserContract
import com.google.firebase.auth.FirebaseAuth

class LoginUserModel : LoginUserContract.Model {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    callback(false, errorMessage)
                }
            }
    }
}