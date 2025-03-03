package com.cnunez.docufast.user.login.Presenter

import com.cnunez.docufast.user.login.Model.LoginUserModel
import com.cnunez.docufast.user.login.Contract.LoginUserContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cnunez.docufast.common.dataclass.User

class LoginUserPresenter(private val view: LoginUserContract.View) : LoginUserContract.Presenter {
    private val model: LoginUserContract.Model = LoginUserModel()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showLoginError("Email y contraseña son obligatorios")
            return
        }

        model.login(email, password) { success, error ->
            if (success) {
                val currentUser = auth.currentUser
                currentUser?.let {
                    db.collection("users").document(it.uid).get()
                        .addOnSuccessListener { document ->
                            val user = document.toObject(User::class.java)
                            user?.let {
                                if (it.role == "admin") {
                                    view.showAdminLoginSuccess(it)
                                } else {
                                    view.showUserLoginSuccess(it)
                                }
                            } ?: run {
                                view.showLoginError("User data is null")
                            }
                        }
                        .addOnFailureListener { exception ->
                            view.showLoginError("Error fetching user data: ${exception.message}")
                        }
                } ?: run {
                    view.showLoginError("Current user is null")
                }
            } else {
                view.showLoginError(error ?: "Error desconocido")
            }
        }
    }
}