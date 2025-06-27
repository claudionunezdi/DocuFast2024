package com.cnunez.docufast.user.login.presenter

import com.cnunez.docufast.user.login.contract.LoginUserContract
import com.cnunez.docufast.user.login.model.LoginUserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.cnunez.docufast.common.dataclass.User

class LoginUserPresenter(
    private val view: LoginUserContract.View
) : LoginUserContract.Presenter {

    private val model: LoginUserContract.Model = LoginUserModel()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showLoginError("Email y contraseña son obligatorios")
            return
        }

        // 1) Autenticarse con FirebaseAuth
        model.login(email, password) { success, error ->
            if (!success) {
                view.showLoginError(error ?: "Error desconocido")
                return@login
            }

            // 2) Si el sign-in fue exitoso, obtener el uid del usuario actual
            val currentUser = auth.currentUser
            if (currentUser == null) {
                view.showLoginError("Error interno: usuario actual nulo")
                return@login
            }

            val uid = currentUser.uid

            // 3) Leer sus datos desde Realtime Database en /users/{uid}
            database.child("users").child(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)
                    if (user == null) {
                        view.showLoginError("No se encontraron datos de usuario")
                        return@addOnSuccessListener
                    }

                    // 4) Discriminar por rol:
                    if (user.role == "ADMIN") {
                        view.showAdminLoginSuccess(user)
                    } else {
                        view.showUserLoginSuccess(user)
                    }
                }
                .addOnFailureListener { e ->
                    view.showLoginError("Error al leer datos de usuario: ${e.message}")
                }
        }
    }
}
