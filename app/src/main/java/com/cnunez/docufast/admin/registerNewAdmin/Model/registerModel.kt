package com.cnunez.docufast.admin.registerNewAdmin.Model

import com.cnunez.docufast.admin.registerNewAdmin.Contract.registerContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class registerModel : registerContract.Model {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun registerUser(
        fullName: String,
        email: String,
        password: String,
        organization: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf(
                            "fullName" to fullName,
                            "email" to email,
                            "organization" to organization,
                            "role" to "admin"
                        )
                        db.collection("users").document(user.uid).set(userData)
                            .addOnSuccessListener {
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                callback(false, e.message)
                            }
                    } else {
                        callback(false, "User registration failed.")
                    }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
}