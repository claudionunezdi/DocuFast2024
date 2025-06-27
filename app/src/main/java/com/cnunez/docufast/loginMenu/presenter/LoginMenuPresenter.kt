package com.cnunez.docufast.loginMenu.presenter

import com.cnunez.docufast.loginMenu.contract.LoginMenuContract

class LoginMenuPresenter(private val view: LoginMenuContract.View) : LoginMenuContract.Presenter {

    override fun onRegisterAdminClicked() {
        view.showRegisterAdmin()
    }



    override fun onLoginUserClicked() {
        view.showLoginUser()
    }
}