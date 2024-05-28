package com.cnunez.docufast.mainMenu.Presenter

import com.cnunez.docufast.mainMenu.Contract.MainMenuContract

class MainMenuPresenter(private val view: MainMenuContract.View) : MainMenuContract.Presenter {

    override fun onGotoCameraButtonClicked() {
        view.gotouseCameraButton()
    }

    override fun onGotoGalleryButtonClicked() {
        view.gotogalleryButton()
    }

    override fun onGotoArchivesButtonClicked() {
        view.gotoarchivesButton()
    }
}
