package com.cnunez.docufast.user.create.MVP

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateUserModel : CreateUserContract.Model {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun createUser(fullName: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                val user = hashMapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "role" to "user"
                )
                userId?.let {
                    db.collection("users").document(it).set(user).addOnSuccessListener {
                        callback(true, null)
                    }.addOnFailureListener { e ->
                        callback(false, e.message)
                    }
                }
            } else {
                callback(false, task.exception?.message)
            }
        }
    }
}