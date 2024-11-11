package com.cnunez.docufast.admin.adminregister.presenter

import com.cnunez.docufast.admin.adminregister.Contract.RegisterUserContract

class RegisterUserPresenter(private val view: RegisterUserContract.View, private val model: RegisterUserContract.Model) : RegisterUserContract.Presenter {

    override fun register(fullName: String, email: String, password: String, organization: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || organization.isBlank()) {
            view.showRegisterError("All fields are required")
            return
        }

        model.createUser(fullName, email, password, organization) { success, errorMessage ->
            if (success) {
                view.showRegisterSuccess()
            } else {
                view.showRegisterError(errorMessage ?: "Unknown error")
            }
        }
    }
}