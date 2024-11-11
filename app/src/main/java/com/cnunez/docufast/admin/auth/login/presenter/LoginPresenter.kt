package com.cnunez.docufast.admin.auth.login.presenter

import com.cnunez.docufast.admin.auth.login.contract.LoginContract



class LoginPresenter(private val view: LoginContract.View, private val model: LoginContract.Model) : LoginContract.Presenter {
    override fun login(email: String, password: String) {
        model.authenticateUser(email, password) { success, message ->
            if (success) {
                view.showLoginSuccess()
            } else {
                view.showLoginError(message ?: "Unknown error")
            }
        }
    }
}