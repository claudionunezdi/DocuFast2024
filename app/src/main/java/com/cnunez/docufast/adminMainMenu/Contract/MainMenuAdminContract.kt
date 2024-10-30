package com.cnunez.docufast.adminMainMenu.Contract

interface MainMenuAdminContract {
    interface View {
        fun showViewGroups()
        fun showViewUsers()
        fun showRegisterNewUser()
        // Add more methods as needed
        fun showCreateGroups()

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