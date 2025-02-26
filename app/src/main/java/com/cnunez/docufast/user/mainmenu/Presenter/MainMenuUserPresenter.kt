package com.cnunez.docufast.user.mainmenu.Presenter

import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract


class MainMenuUserPresenter(private val view: MainMenuUserContract.View) : MainMenuUserContract.Presenter {
    override fun onViewWorkGroupsClicked() {
        view.showWorkGroups()
    }

}