package com.cnunez.docufast.loginMenu.Contract

interface LoginContract {
    interface View {
        fun showRegisterAdmin()
        fun showLoginAdmin()
        fun showLoginUser()
    }

    interface Presenter {
        fun onRegisterAdminClicked()
        fun onLoginAdminClicked()
        fun onLoginUserClicked()
    }
}