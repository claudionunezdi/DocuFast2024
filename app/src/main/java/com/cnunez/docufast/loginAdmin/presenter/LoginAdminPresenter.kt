package com.cnunez.docufast.loginAdmin.presenter

import com.cnunez.docufast.loginAdmin.contract.LoginAdminContract



class LoginAdminPresenter(private val view: LoginAdminContract.View, private val model: LoginAdminContract.Model) : LoginAdminContract.Presenter {
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