package com.cnunez.docufast.useCamera.Contract

import android.widget.Button
import com.google.common.util.concurrent.ListenableFuture



interface CameraContract {
    interface CameraView {
        fun showCameraPreview()
        fun showError(message: String)
        fun showPhotoTaken(photoPath: String)
        fun onTakePhotoClicked()
    }

    interface CameraPresenter {
        fun onTakePhotoClicked()
        fun onPhotoCaptured(photoPath: String)
    }

    interface CameraModel {
        fun takePhoto(callback: (String?) -> Unit)
    }
}
