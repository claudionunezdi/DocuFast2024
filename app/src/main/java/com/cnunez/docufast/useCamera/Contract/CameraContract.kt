package com.cnunez.docufast.useCamera.Contract

import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy

interface CameraContract {
    interface CameraView {
        fun showError(message: String)
        fun showPhotoTaken(photoPath: String)
        fun showOcrResult(text: String)
        fun showSuccess(message: String)
        fun showImage(imageUri: Uri)



        fun showEditFileNameDialog(fileId: Int, callback: (String) -> Unit)
    }

    interface CameraPresenter {

        fun onFileNameConfirmed(fileId: Int, newFileName: String)
        fun onCaptureButtonClicked()
        fun onApplyOcrButtonClicked()
        fun onSaveTextButtonClicked(text: String)
        @OptIn(ExperimentalGetImage::class)
        fun analyzer(imageProxy: ImageProxy)
    }

    interface CameraModel {

        fun savePhoto()
        fun takePhoto(callback: (Uri?) -> Unit)
        fun applyOcr(photoUri: Uri, callback: (String?) -> Unit)
        fun saveTextToFile(text: String, callback: (Boolean, String?) -> Unit)
        fun showPhotoTaken(photoPath: String)
    }
}
