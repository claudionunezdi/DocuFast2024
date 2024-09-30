package com.cnunez.docufast.camera.contract

import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.cnunez.docufast.camera.model.TextFile

interface CameraContract {
    interface CameraView {
        fun showError(message: String)
        fun showPhotoTaken(photoPath: String)
        fun showOcrResult(text: String)
        fun showSuccess(message: String)
        fun showImage(imageUri: Uri)
        fun showEditFileNameDialog(fileId: Int, callback: (String) -> Unit)
        fun showAllTextFiles(textFiles: List<TextFile>)
        fun showTextFile(textFile: TextFile)
    }

    interface CameraPresenter {
        fun onFileNameConfirmed(fileName: String, text: String)
        fun onFileNameEdited(fileId: Int, newFileName: String)
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