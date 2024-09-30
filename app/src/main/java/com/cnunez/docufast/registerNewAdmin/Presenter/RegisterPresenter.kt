package com.cnunez.docufast.registerNewAdmin.Presenter

import com.cnunez.docufast.registerNewAdmin.Contract.RegisterContract

class RegisterPresenter(private val view: RegisterContract.View, private val model: RegisterContract.Model) : RegisterContract.Presenter {
    override fun register(fullName: String, email: String, password: String, organization: String) {
        model.createUser(fullName, email, password, organization) { success, message ->
            if (success) {
                view.showRegisterSuccess()
            } else {
                view.showRegisterError(message ?: "Unknown error")
            }
        }
    }
}