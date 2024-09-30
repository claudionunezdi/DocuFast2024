package com.cnunez.docufast.loginUser

interface LoginUserContract {
    interface View {
        fun showLoginSuccess()
        fun showLoginError(error: String)
    }

    interface Presenter {
        fun loginUser(username: String, password: String)
    }

    interface Model {
        fun authenticate(username: String, password: String, callback: (Boolean, String?) -> Unit)
    }
}