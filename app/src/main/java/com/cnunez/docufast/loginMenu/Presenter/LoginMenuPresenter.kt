package com.cnunez.docufast.loginMenu.Presenter

import com.cnunez.docufast.loginMenu.Contract.LoginMenuContract

class LoginMenuPresenter(private val view: LoginMenuContract.View) : LoginMenuContract.Presenter {

    override fun onRegisterAdminClicked() {
        view.showRegisterAdmin()
    }



    override fun onLoginUserClicked() {
        view.showLoginUser()
    }
}