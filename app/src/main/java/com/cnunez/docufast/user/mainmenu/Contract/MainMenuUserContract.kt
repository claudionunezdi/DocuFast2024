package com.cnunez.docufast.user.mainmenu.Contract

class MainMenuUserContract {
    interface View{
        fun showWorkGroups()

    }

    interface Presenter{
        fun onViewWorkGroupsClicked()

    }
}