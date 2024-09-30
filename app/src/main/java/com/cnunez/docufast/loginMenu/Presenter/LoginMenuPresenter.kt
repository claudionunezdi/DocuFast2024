package com.cnunez.docufast.loginMenu.Presenter

import com.cnunez.docufast.loginMenu.Contract.LoginContract

class LoginMenuPresenter(private val view: LoginContract.View) : LoginContract.Presenter {

    override fun onRegisterAdminClicked() {
        view.showRegisterAdmin()
    }

    override fun onLoginAdminClicked() {
        view.showLoginAdmin()
    }

    override fun onLoginUserClicked() {
        view.showLoginUser()
    }
}