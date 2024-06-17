package com.cnunez.docufast.useCamera.Presenter

import android.net.Uri
import com.cnunez.docufast.useCamera.Contract.CameraContract

class CameraPresenter(
    private val cameraModel: CameraContract.CameraModel,
    private val cameraView: CameraContract.CameraView


) : CameraContract.CameraPresenter {

    private var photoUri: Uri? = null

    override fun onCaptureButtonClicked() {
        cameraModel.takePhoto { photoUri ->
            if (photoUri != null) {
                this.photoUri = photoUri
                cameraView.showPhotoTaken(photoUri.toString())
            } else {
                cameraView.showError("Error taking photo")
            }
        }
    }

    override fun onApplyOcrButtonClicked() {
        photoUri?.let { uri ->
            cameraModel.applyOcr(uri) { text ->
                if (text != null) {
                    cameraView.showOcrResult(text)
                } else {
                    cameraView.showError("Error applying OCR")
                }
            }
        } ?: cameraView.showError("No photo to apply OCR")
    }

    override fun onSaveTextButtonClicked(text: String) {
        if (text.isNotEmpty()) {
            cameraModel.saveTextToFile(text) { success, filePath
                ->
                if (success) {
                    cameraView.showSuccess("Text saved to $filePath")
                } else {
                    cameraView.showError("Error saving text: $filePath")
                }
            }
        } else {
            cameraView.showError("No text to save")
        }
    }
}
