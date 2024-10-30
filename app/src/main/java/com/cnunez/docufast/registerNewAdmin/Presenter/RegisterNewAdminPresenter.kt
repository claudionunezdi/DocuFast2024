package com.cnunez.docufast.registerNewAdmin.Presenter

import com.cnunez.docufast.registerNewAdmin.Contract.RegisterNewAdminContract

class RegisterNewAdminPresenter(private val view: RegisterNewAdminContract.View, private val model: RegisterNewAdminContract.Model) : RegisterNewAdminContract.Presenter {

    override fun register(fullName: String, email: String, password: String, organization: String) {
        model.register(fullName, email, password, organization, object : RegisterNewAdminContract.RegisterListener {
            override fun onSuccess() {
                view.showRegisterSuccess()
            }

            override fun onError(message: String) {
                view.showRegisterError(message)
            }
        })
    }
}