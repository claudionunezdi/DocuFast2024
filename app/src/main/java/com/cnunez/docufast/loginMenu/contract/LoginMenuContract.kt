package com.cnunez.docufast.loginMenu.contract

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