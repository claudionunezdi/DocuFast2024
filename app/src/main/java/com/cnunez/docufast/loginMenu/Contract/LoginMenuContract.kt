package com.cnunez.docufast.loginMenu.Contract

interface LoginMenuContract {
    interface View {
        fun showRegisterAdmin()

        fun showLoginUser()
    }

    interface Presenter {
        fun onRegisterAdminClicked()

        fun onLoginUserClicked()
    }
}