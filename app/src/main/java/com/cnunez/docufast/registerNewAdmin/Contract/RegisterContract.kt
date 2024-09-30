package com.cnunez.docufast.registerNewAdmin.Contract

interface RegisterContract {
    interface View {
        fun showRegisterSuccess()
        fun showRegisterError(message: String)
    }

    interface Presenter {
        fun register(fullName: String, email: String, password: String, organization: String)
    }

    interface Model {

        fun createUser(fullName: String, email: String, password: String, organization: String, callback: (Boolean, String?) -> Unit)
    }
}