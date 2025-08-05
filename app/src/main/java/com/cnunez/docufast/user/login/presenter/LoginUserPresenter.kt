package com.cnunez.docufast.user.login.presenter

import android.util.Log
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.user.login.contract.LoginUserContract
import com.cnunez.docufast.user.login.model.LoginUserModel
import com.google.firebase.auth.FirebaseAuth

class LoginUserPresenter(
    private val view: LoginUserContract.View
) : LoginUserContract.Presenter {

    private val model: LoginUserContract.Model = LoginUserModel()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showLoginError("Email y contraseña son obligatorios")
            return
        }

        Log.d("LoginFlow", "Iniciando autenticación para: $email")
        model.login(email, password) { success, error ->
            if (!success) {
                Log.e("LoginFlow", "Error de autenticación: $error")
                view.showLoginError(error ?: "Error desconocido")
                return@login
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                view.showLoginError("Error interno: usuario actual nulo")
                return@login
            }

            Log.d("LoginFlow", "Usuario autenticado, obteniendo datos para UID: ${currentUser.uid}")
            (model as LoginUserModel).getUserData(currentUser.uid) { user, error ->
                if (user == null) {
                    Log.e("LoginFlow", "Error obteniendo datos: $error")
                    view.showLoginError(error ?: "Datos de usuario no encontrados")
                    return@getUserData
                }

                Log.d("LoginFlow", "Rol detectado: ${user.role}")
                when (user.role?.uppercase()) {
                    "ADMIN" -> {
                        Log.i("LoginFlow", "Redirigiendo a menú de administrador")
                        view.showAdminLoginSuccess(user)
                    }
                    "USER" -> {
                        Log.i("LoginFlow", "Redirigiendo a menú de usuario")
                        view.showUserLoginSuccess(user)
                    }
                    else -> {
                        Log.e("LoginFlow", "Rol no reconocido: ${user.role}")
                        view.showLoginError("Tu cuenta no tiene un rol válido asignado")
                        auth.signOut()
                    }
                }
            }
        }
    }
}