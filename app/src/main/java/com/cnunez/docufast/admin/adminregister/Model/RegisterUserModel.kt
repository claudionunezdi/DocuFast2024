package com.cnunez.docufast.admin.adminregister.model

import com.cnunez.docufast.admin.adminregister.Contract.RegisterUserContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterUserModel : RegisterUserContract.Model {

    override fun createUser(fullName: String, email: String, password: String, organization: String, callback: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "organization" to organization
                    )

                    db.collection("users").document(userId)
                        .set(user)
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