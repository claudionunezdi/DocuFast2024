package com.cnunez.docufast.loginAdmin.model

import com.cnunez.docufast.loginAdmin.contract.LoginAdminContract
import com.google.firebase.auth.FirebaseAuth

class LoginAdminModel : LoginAdminContract.Model {
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