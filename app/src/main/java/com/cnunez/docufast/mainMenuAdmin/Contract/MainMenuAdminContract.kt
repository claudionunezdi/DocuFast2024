package com.cnunez.docufast.mainMenuAdmin.Contract

interface MainMenuAdminContract {
    interface View {
        fun showViewGroups()
        fun showViewUsers()
        fun showRegisterNewUser()
    }

    interface Presenter {
        fun viewGroups()
        fun viewUsers()
        fun registerNewUser()
    }
    interface Model {
        // Implement the necessary methods for the model
    }
}