package com.cnunez.docufast.admin.registerNewAdmin.Model

import com.cnunez.docufast.admin.registerNewAdmin.Contract.registerContract
import com.google.firebase.auth.FirebaseAuth

class registerModel : registerContract.Model {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun register(fullName: String, email: String, password: String, organization: String, listener: registerContract.RegisterListener) {
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