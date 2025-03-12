package com.cnunez.docufast.admin.user.create.presenter

import android.util.Log
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateUserPresenter(
    private val view: CreateUserContract.View,
    private val model: CreateUserContract.Model
) : CreateUserContract.Presenter {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun createUser(username: String, email: String, password: String, workGroups: MutableList<String>) {
        val currentUser = auth.currentUser

        currentUser?.let {
            db.collection("users").document(it.uid).get().addOnSuccessListener { document ->
                val userRole = document.getString("role")
                val isAdmin = userRole == "admin"
                Log.d("UserRoleCheck", "Is user admin: $isAdmin")

                if (isAdmin) {
                    val organization = document.getString("organization") ?: ""
                    Log.d("CreateUser", "Checking if email is already in use: $email")

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val emailExists = db.collection("users").whereEqualTo("email", email).get().await().isEmpty
                            if (!emailExists) {
                                Log.d("CreateUser", "Email not in use, creating user: $email")
                                val createTask = auth.createUserWithEmailAndPassword(email, password).await()
                                val newUser = createTask.user
                                newUser?.let { newUser ->
                                    val user = User(
                                        id = newUser.uid,
                                        name = username,
                                        email = email,
                                        organization = organization,
                                        workGroups = workGroups,
                                        role = "user"
                                    )
                                    db.collection("users").document(newUser.uid).set(user).await()
                                    view.showCreateUserSuccess()
                                }
                            } else {
                                Log.e("CreateUser", "The email address is already in use by another account.")
                                view.showCreateUserError("The email address is already in use by another account.")
                            }
                        } catch (e: Exception) {
                            Log.e("CreateUser", "Error checking email: ${e.message}")
                            view.showCreateUserError(e.message ?: "Error checking email")
                        }
                    }
                } else {
                    Log.e("CreateUser", "No tienes permisos suficientes para crear un usuario")
                    view.showCreateUserError("No tienes permisos suficientes para crear un usuario")
                }
            }
        }
    }
}