package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class UserManager {
    private val users = mutableListOf<User>()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createUser(
        id: String,
        name: String,
        email: String,
        password: String,
        organization: String,
        workGroups: List<String>,
        role: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = User(
                        id = id,
                        name = name,
                        email = email,
                        password = password,
                        organization = organization,
                        workGroups = workGroups.toMutableList(),
                        role = role
                    )
                    users.add(user)
                    // Store user data in Firestore
                    val userData = hashMapOf(
                        "id" to id,
                        "name" to name,
                        "email" to email,
                        "organization" to organization,
                        "workGroups" to workGroups,
                        "role" to role
                    )
                    db.collection("users").document(id).set(userData)
                        .addOnSuccessListener {
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            onComplete(false, e.message)
                        }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun signInUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun addUserToAdmin(adminUser: User, user: User) {
        val admin = users.find { it.email == adminUser.email }
        admin?.workGroups?.addAll(user.workGroups)
    }
}