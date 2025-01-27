package com.cnunez.docufast.user.create.MVP

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateUserModel : CreateUserContract.Model {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun createUser(
        username: String,
        email: String,
        password: String,
        workgroups: List<String>,
        organization: String,
        role: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                val user = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "workgroups" to workgroups,
                    "organization" to organization,
                    "role" to role
                )
                userId?.let {
                    db.collection("users").document(it).set(user).addOnSuccessListener {
                        callback(true, null)
                    }.addOnFailureListener { e ->
                        callback(false, e.message)
                    }
                } ?: run {
                    callback(false, "User ID is null")
                }
            } else {
                callback(false, task.exception?.message)
            }
        }
    }
}