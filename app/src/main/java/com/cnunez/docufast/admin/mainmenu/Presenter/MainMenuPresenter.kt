package com.cnunez.docufast.admin.mainmenu.Presenter

import com.cnunez.docufast.admin.mainmenu.Contract.MainMenuContract
import com.google.firebase.auth.FirebaseAuth

class MainMenuPresenter(private val view: MainMenuContract.View) : MainMenuContract.Presenter {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun viewGroups() {
        view.showViewGroups()
    }

    override fun viewUsers() {
        view.showViewUsers()
    }

    override fun registerNewUser() {
        view.showRegisterNewUser()
    }

    override fun getUserName(): String {
        return auth.currentUser?.displayName ?: "Unknown"

    }
}