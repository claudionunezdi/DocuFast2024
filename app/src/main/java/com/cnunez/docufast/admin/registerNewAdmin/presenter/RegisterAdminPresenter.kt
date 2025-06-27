// RegisterAdminPresenter.kt
package com.cnunez.docufast.admin.registerNewAdmin.presenter

import com.cnunez.docufast.admin.registerNewAdmin.contract.RegisterAdminContract

class RegisterAdminPresenter(
    private val view: RegisterAdminContract.View,
    private val model: RegisterAdminContract.Model
) : RegisterAdminContract.Presenter {

    override fun registerAdmin(
        fullName: String,
        email: String,
        password: String,
        organization: String
    ) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || organization.isBlank()) {
            view.showRegisterError("Todos los campos son obligatorios.")
            return
        }
        view.showLoading()
        model.registerAdmin(fullName, email, password, organization) { success, message ->
            view.hideLoading()
            if (success) {
                view.showRegisterSuccess()
            } else {
                view.showRegisterError(message ?: "Error desconocido.")
            }
        }
    }

    override fun createUser(
        fullName: String,
        email: String,
        password: String,
        organization: String
    ) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || organization.isBlank()) {
            view.showUserCreateError("Todos los campos son obligatorios.")
            return
        }
        view.showLoading()
        model.createUser(fullName, email, password, organization) { success, message ->
            view.hideLoading()
            if (success) {
                view.showUserCreateSuccess()
            } else {
                view.showUserCreateError(message ?: "Error desconocido.")
            }
        }
    }
}
