package com.cnunez.docufast.adminMainMenu.Presenter

import com.cnunez.docufast.adminMainMenu.Contract.MainMenuAdminContract

class MainMenuAdminPresenter(private val view: MainMenuAdminContract.View) : MainMenuAdminContract.Presenter {
    override fun viewGroups() {
        view.showViewGroups()
    }

    override fun viewUsers() {
        view.showViewUsers()
    }

    override fun registerNewUser() {
        view.showRegisterNewUser()
    }
}