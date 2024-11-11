package com.cnunez.docufast.userLogin

interface LoginContract {
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