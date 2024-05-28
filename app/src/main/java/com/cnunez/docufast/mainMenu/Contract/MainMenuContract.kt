package com.cnunez.docufast.mainMenu.Contract

interface MainMenuContract {
    interface View {
        fun gotouseCameraButton()
        fun gotogalleryButton()
        fun gotoarchivesButton()
    }

    interface Presenter {
        fun onGotoCameraButtonClicked()
        fun onGotoGalleryButtonClicked()
        fun onGotoArchivesButtonClicked()
    }
}
