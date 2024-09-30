package com.cnunez.docufast.loginUser.Presenter

import com.cnunez.docufast.loginUser.LoginUserContract
import com.cnunez.docufast.loginUser.Model.LoginUserModel

class LoginUserPresenter(private val view: LoginUserContract.View) : LoginUserContract.Presenter {
    private val model: LoginUserContract.Model = LoginUserModel()

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