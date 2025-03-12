package com.cnunez.docufast.admin.user.create.model

import android.content.Context
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateUserModel(private val context: Context) : CreateUserContract.Model {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun createUser(
        username: String,
        email: String,
        password: String,
        workGroups: MutableList<String>,
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
                    "workGroups" to workGroups,
                    "organization" to organization,
                    "role" to role
                )
                userId?.let {
                    db.collection("users").document(it).set(user).addOnSuccessListener {
                        // Save user role in SharedPreferences
                        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().apply {
                            putString("userRole", role)
                            apply()
                        }
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