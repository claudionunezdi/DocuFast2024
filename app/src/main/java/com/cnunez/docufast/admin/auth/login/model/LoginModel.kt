package com.cnunez.docufast.admin.auth.login.model

import com.cnunez.docufast.admin.auth.login.contract.LoginContract
import com.google.firebase.auth.FirebaseAuth

class LoginModel : LoginContract.Model {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun authenticateUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
}