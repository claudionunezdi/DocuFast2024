package com.cnunez.docufast.admin.mainmenu.Contract

interface MainMenuContract {
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