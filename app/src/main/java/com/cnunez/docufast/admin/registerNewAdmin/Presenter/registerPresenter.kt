package com.cnunez.docufast.admin.registerNewAdmin.Presenter

import com.cnunez.docufast.admin.registerNewAdmin.Contract.registerContract

class registerPresenter(private val view: registerContract.View, private val model: registerContract.Model) : registerContract.Presenter {

    override fun register(fullName: String, email: String, password: String, organization: String) {
        model.register(fullName, email, password, organization, object : registerContract.RegisterListener {
            override fun onSuccess() {
                view.showRegisterSuccess()
            }

            override fun onError(message: String) {
                view.showRegisterError(message)
            }
        })
    }
}