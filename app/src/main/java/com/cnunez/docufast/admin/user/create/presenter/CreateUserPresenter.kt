package com.cnunez.docufast.admin.user.create.presenter

import android.util.Log
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.EmailAuthProvider
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

    override fun createUserWithAdminPassword(
        username: String,
        email: String,
        password: String,
        workGroups: MutableList<String>,
        adminPassword: String
    ) {
        val currentUser = auth.currentUser
        val adminEmail = currentUser?.email

        currentUser?.let {
            db.collection("users").document(it.uid).get().addOnSuccessListener { document ->
                val userRole = document.getString("role")
                val isAdmin = userRole == "admin"

                if (isAdmin) {
                    val organization = document.getString("organization") ?: ""

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val emailExistsInFirestore = db.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .await()
                                .isEmpty.not()

                            if (emailExistsInFirestore) {
                                view.showCreateUserError("El correo ya está registrado en Firestore.")
                                return@launch
                            }

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

                                // Reautenticar al administrador
                                adminEmail?.let {
                                    val credential = EmailAuthProvider.getCredential(adminEmail, adminPassword)
                                    auth.signInWithCredential(credential).await()
                                }

                                view.showCreateUserSuccess()
                            } ?: run {
                                view.showCreateUserError("Error al crear el usuario en Firebase Authentication.")
                            }
                        } catch (e: Exception) {
                            view.showCreateUserError(e.message ?: "Error desconocido.")
                        }
                    }
                } else {
                    view.showCreateUserError("No tienes permisos suficientes para crear un usuario.")
                }
            }
        }
    }
}