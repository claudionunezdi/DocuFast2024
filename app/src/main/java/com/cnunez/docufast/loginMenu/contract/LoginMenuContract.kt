package com.cnunez.docufast.loginMenu.contract

import com.cnunez.docufast.common.dataclass.User

interface LoginMenuContract {
    interface View {
        fun showRegisterAdmin()
        fun showLoginUser()
        fun showMainMenu(user: User)
        fun showLoading(show: Boolean)
        fun showError(message: String)
    }

    interface Presenter {
        fun onRegisterAdminClicked()
        fun onLoginUserClicked()
        fun checkActiveSession()
    }

    interface Model {
        fun authenticateUser(email: String, password: String, callback: (User?, String?) -> Unit)
        fun checkCurrentSession(callback: (User?) -> Unit)
    }
}