package com.cnunez.docufast.useCamera.Contract

import android.net.Uri

interface CameraContract {
    interface CameraView {
        fun showError(message: String)
        fun showPhotoTaken(photoPath: String)
        fun showOcrResult(text: String)
        fun showSuccess(message: String)
    }

    interface CameraPresenter {
        fun onCaptureButtonClicked()
        fun onApplyOcrButtonClicked()
        fun onSaveTextButtonClicked(text: String)
    }

    interface CameraModel {
        fun takePhoto(callback: (Uri?) -> Unit)
        fun applyOcr(photoUri: Uri, callback: (String?) -> Unit)
        fun saveTextToFile(text: String, callback: (Boolean, String?) -> Unit)
    }
}
