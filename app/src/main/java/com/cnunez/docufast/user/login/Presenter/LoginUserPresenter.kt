package com.cnunez.docufast.user.login.Presenter
import com.cnunez.docufast.user.login.Model.LoginUserModel
import com.cnunez.docufast.user.login.Contract.LoginUserContract


class LoginUserPresenter(private val view: LoginUserContract.View) : LoginUserContract.Presenter {
    private val model: LoginUserContract.Model = LoginUserModel()

    override fun login(email: String, password: String) {
        model.login(email, password) { success, error ->
            if (success) {
                view.showLoginSuccess()
            } else {
                view.showLoginError(error ?: "Unknown error")
            }
        }
    }
}