package com.cnunez.docufast.loginMenu.presenter

import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.loginMenu.contract.LoginMenuContract

class LoginMenuPresenter(
    private val view: LoginMenuContract.View,
    private val model: LoginMenuContract.Model
) : LoginMenuContract.Presenter {

    override fun onRegisterAdminClicked() {
        view.showRegisterAdmin()
    }

    override fun onLoginUserClicked() {
        view.showLoginUser()
    }


    override fun checkActiveSession() {
        model.checkCurrentSession { user ->
            user?.let {
                view.showMainMenu(it)
            }
        }
    }
}