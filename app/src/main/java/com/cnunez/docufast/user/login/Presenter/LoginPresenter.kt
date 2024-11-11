package com.cnunez.docufast.user.login.Presenter

import com.cnunez.docufast.userLogin.LoginContract
import com.cnunez.docufast.user.login.Model.LoginModel

class LoginPresenter(private val view: LoginContract.View) : LoginContract.Presenter {
    private val model: LoginContract.Model = LoginModel()

    override fun loginUser(username: String, password: String) {
        model.authenticate(username, password) { success, error ->
            if (success) {
                view.showLoginSuccess()
            } else {
                view.showLoginError(error ?: "Unknown error")
            }
        }
    }
}