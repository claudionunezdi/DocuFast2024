package com.cnunez.docufast.user.login.Contract



interface LoginUserContract {
    interface View {
        fun showLoginSuccess()
        fun showLoginError(message: String)
    }

    interface Presenter {
        fun login(email: String, password: String)
    }

    interface Model {
        fun login(email: String, password: String, callback: (Boolean, String?) -> Unit)
    }
}