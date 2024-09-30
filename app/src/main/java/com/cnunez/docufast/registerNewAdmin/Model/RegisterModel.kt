package com.cnunez.docufast.registerNewAdmin.Model

import com.cnunez.docufast.registerNewAdmin.Contract.RegisterContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterModel : RegisterContract.Model {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun createUser(fullName: String, email: String, password: String, organization: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "organization" to organization,
                        "isAdmin" to true
                    )
                    firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            callback(true, null)
                        }
                        .addOnFailureListener { e ->
                            callback(false, e.message)
                        }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
}