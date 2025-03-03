package com.cnunez.docufast.user.login.Contract

import com.cnunez.docufast.common.dataclass.User

interface LoginUserContract {
    interface View {
        fun showAdminLoginSuccess(user: User)
        fun showUserLoginSuccess(user: User)
        fun showLoginError(message: String)
    }

    interface Presenter {
        fun login(email: String, password: String)
    }

    interface Model {
        fun login(email: String, password: String, callback: (Boolean, String?) -> Unit)
    }
}