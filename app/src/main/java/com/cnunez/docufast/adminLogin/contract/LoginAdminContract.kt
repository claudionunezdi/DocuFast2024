package com.cnunez.docufast.adminLogin.contract

interface LoginAdminContract {
    interface View {
        fun showLoginSuccess()
        fun showLoginError(message: String)
    }

    interface Presenter {
        fun login(email: String, password: String)
    }

    interface Model {
        fun authenticateUser(email: String, password: String, callback: (Boolean, String?) -> Unit)
    }
}