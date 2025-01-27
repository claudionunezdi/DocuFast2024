package com.cnunez.docufast.admin.registerNewAdmin.Model

import android.util.Log
import com.cnunez.docufast.admin.registerNewAdmin.Contract.registerContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class registerModel : registerContract.Model {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun createAdmin(fullName: String, email: String, password: String, organization: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                val user = hashMapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "role" to "admin",
                    "organization" to organization
                )
                userId?.let {
                    db.collection("users").document(it).set(user).addOnSuccessListener {
                        Log.d("registerModel", "User created with ID: $userId")
                        callback(true, null)
                    }.addOnFailureListener { e ->
                        Log.e("registerModel", "Error creating user: ${e.message}")
                        callback(false, e.message)
                    }
                } ?: run {
                    Log.e("registerModel", "User ID is null")
                    callback(false, "User ID is null")
                }
            } else {
                Log.e("registerModel", "Error creating user: ${task.exception?.message}")
                callback(false, task.exception?.message)
            }
        }
    }
}