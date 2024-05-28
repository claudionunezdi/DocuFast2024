package com.cnunez.docufast.useCamera.Presenter

import com.cnunez.docufast.useCamera.Contract.CameraContract
import com.cnunez.docufast.useCamera.Model.CameraModel

class CameraPresenter(
    private val cameraModel: CameraModel,
    private val cameraView: CameraContract.CameraView
) : CameraContract.CameraPresenter {

    override fun onTakePhotoClicked() {
        cameraModel.takePhoto { photoPath ->
            if (photoPath != null) {
                cameraView.showPhotoTaken(photoPath)
            } else {
                cameraView.showError("Error taking photo")
            }
        }
    }

    override fun onPhotoCaptured(photoPath: String) {
        cameraView.showPhotoTaken(photoPath)
    }
}
