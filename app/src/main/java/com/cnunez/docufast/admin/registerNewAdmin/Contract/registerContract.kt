package com.cnunez.docufast.admin.registerNewAdmin.Contract

interface registerContract {
    interface View {
        fun showRegisterSuccess()
        fun showRegisterError(message: String)
    }

    interface Presenter {
        fun register(fullName: String, email: String, password: String, organization: String)
    }

    interface Model {
        fun register(fullName: String, email: String, password: String, organization: String, listener: RegisterListener)
    }

    interface RegisterListener {
        fun onSuccess()
        fun onError(message: String)
    }
}