package com.cnunez.docufast.admin.mainmenu.Presenter

import com.cnunez.docufast.admin.mainmenu.Contract.MainMenuContract

class MainMenuPresenter(private val view: MainMenuContract.View) : MainMenuContract.Presenter {
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