package com.cnunez.docufast.admin.mainmenu.Contract

interface MainMenuContract {
    interface View {
        fun showViewGroups()
        fun showViewUsers()
        fun showRegisterNewUser()


    }

    interface Presenter {
        fun viewGroups()
        fun viewUsers()
        fun registerNewUser()
        fun getUserName(): String
    }
    interface Model {
        // Implement the necessary methods for the model
    }
}