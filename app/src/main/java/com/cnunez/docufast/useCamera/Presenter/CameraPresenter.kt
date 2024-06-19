package com.cnunez.docufast.useCamera.Presenter

import android.net.Proxy
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import com.cnunez.docufast.useCamera.Contract.CameraContract
import com.cnunez.docufast.useCamera.Model.CameraModel
import com.cnunez.docufast.useCamera.Model.Impl.CameraModelImpl
import com.cnunez.docufast.useCamera.Model.Photo
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

 class CameraPresenter(
    private val cameraModel: CameraContract.CameraModel,
    private val cameraView: CameraContract.CameraView,
) : CameraContract.CameraPresenter {
     private var photoUri: Uri? = null

     private fun degreesToFirebaseRotation(degrees: Int): Int {
        return when (degrees) {
            0 -> 0
            90 -> 90
            180 -> 180
            270 -> 270
            else -> throw IllegalArgumentException("Rotation must be 0, 90, 180, or 270.")
        }
    }

     override fun onFileNameConfirmed(fileId: Int, newFileName: String) {
         TODO("Not yet implemented")
     }


     override fun onCaptureButtonClicked() {
         cameraModel.takePhoto { uri ->
             if (uri != null) {
                 photoUri = uri
                 cameraView.showPhotoTaken(uri.toString())
             } else {
                 cameraView.showError("Error taking photo")
             }
         }
     }



     override fun onApplyOcrButtonClicked() {
         if (photoUri != null) {
             cameraModel.applyOcr(photoUri!!) { text ->
                 if (text != null) {
                     cameraView.showOcrResult(text)
                 } else {
                     cameraView.showError("Error applying OCR")
                 }
             }
         } else {
             cameraView.showError("No photo taken yet")
         }
     }

    override fun onSaveTextButtonClicked(text: String) {
        if (text.isNotEmpty()) {
            cameraModel.saveTextToFile(text) { success, filePath ->
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

     @OptIn(ExperimentalGetImage::class)
     override fun analyzer(imageProxy: ImageProxy) {
            val image = imageProxy.image
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val imageRotation = degreesToFirebaseRotation(rotationDegrees)
            val imageToProcess = InputImage.fromMediaImage(image!!, imageRotation)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(imageToProcess)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    cameraView.showOcrResult(text)
                }
                .addOnFailureListener { e ->
                    cameraView.showError("Error processing image: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
     }
 }