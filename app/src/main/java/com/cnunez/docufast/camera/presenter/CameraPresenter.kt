package com.cnunez.docufast.camera.presenter

import android.content.Context
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.camera.model.TextFile
import com.cnunez.docufast.common.firebase.TextFileDaoFirebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CameraPresenter(
    private val context: Context,
    private val cameraModel: CameraContract.CameraModel,
    private val cameraView: CameraContract.CameraView,
    private val textFileDaoFirebase: TextFileDaoFirebase
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

    override fun onFileNameConfirmed(fileName: String, text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File("${context.getExternalFilesDir(null)}/$fileName")
            file.writeText(text)
            val textFile = TextFile(uri = file.toURI().toString(), content = text, fileName = fileName)
            textFileDaoFirebase.insert(textFile)
        }
    }

    override fun onFileNameEdited(fileId: Int, newFileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val textFile = textFileDaoFirebase.getTextFileById(fileId.toString())
            textFile?.let {
                it.fileName = newFileName
                textFileDaoFirebase.update(it)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val oldFile = File("${context.getExternalFilesDir(null)}/${textFileDaoFirebase.getTextFileById(fileId.toString())?.fileName}")
            val newFile = File("${context.getExternalFilesDir(null)}/$newFileName")
            oldFile.renameTo(newFile)
        }
    }

    override fun onCaptureButtonClicked() {
        cameraModel.takePhoto { uri: Uri? ->
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
            cameraModel.applyOcr(photoUri!!) { text: String? ->
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
            cameraModel.saveTextToFile(text) { success: Boolean, filePath: String? ->
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

    @ExperimentalGetImage
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