package com.cnunez.docufast.admin.registerNewAdmin.Contract

interface registerContract {
    interface View {
        fun showRegisterSuccess()
        fun showRegisterError(error: String)
    }

    interface Presenter {
        fun register(fullName: String, email: String, password: String, organization: String)
    }

    interface Model {
        fun createAdmin(fullName: String, email: String, password: String, organization: String, callback: (Boolean, String?) -> Unit)
    }
}
