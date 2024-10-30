package com.cnunez.docufast.registerNewAdmin.Model

import com.cnunez.docufast.registerNewAdmin.Contract.RegisterNewAdminContract
import com.google.firebase.auth.FirebaseAuth

class RegisterNewAdminModel : RegisterNewAdminContract.Model {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun register(fullName: String, email: String, password: String, organization: String, listener: RegisterNewAdminContract.RegisterListener) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    listener.onSuccess()
                } else {
                    listener.onError(task.exception?.message ?: "Registration failed")
                }
            }
    }
}