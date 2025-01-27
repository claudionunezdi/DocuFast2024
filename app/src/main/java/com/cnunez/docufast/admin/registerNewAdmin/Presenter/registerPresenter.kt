package com.cnunez.docufast.admin.registerNewAdmin.Presenter

import com.cnunez.docufast.admin.registerNewAdmin.Contract.registerContract
import com.cnunez.docufast.admin.registerNewAdmin.Model.registerModel

class registerPresenter(private val view: registerContract.View, private val model: registerContract.Model) : registerContract.Presenter {

    override fun register(fullName: String, email: String, password: String, organization: String) {
        model.createAdmin(fullName, email, password, organization) { success: Boolean, error: String? ->
            if (success) {
                view.showRegisterSuccess()
            } else {
                view.showRegisterError(error ?: "Unknown error")
            }
        }
    }
}