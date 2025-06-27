package com.cnunez.docufast.camera.presenter

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.cnunez.docufast.camera.contract.CameraContract

class CameraPresenter(
    private val view: CameraContract.View,
    private val model: CameraContract.Model
) : CameraContract.Presenter {

    private var lastBitmap: Bitmap? = null
    private var lastImageFileId: String? = null
    private val currentGroupId: String = "grupo-id-ejemplo"

    override fun capturePhoto() {
        // Ya se llama desde la actividad, puede omitirse aquí si no se usa
    }

    override fun applyOcr(bitmap: Bitmap) {
        lastBitmap = bitmap
        model.recognizeTextFromBitmap(bitmap) { text, error ->
            if (error != null) {
                view.showError(error)
            } else if (text != null) {
                view.showOcrResult(text)
            }
        }
    }

    override fun saveOcrText(fileName: String) {
        val bitmap = lastBitmap ?: return view.showError("No hay imagen para guardar")
        model.saveImageToStorage(bitmap, currentGroupId) { imageFile, error ->
            if (error != null || imageFile == null) {
                view.showError(error ?: "Error al guardar imagen")
                return@saveImageToStorage
            }

            lastImageFileId = imageFile.id
            view.showImageSaved(imageFile)

            model.recognizeTextFromBitmap(bitmap) { text, error2 ->
                if (error2 != null || text == null) {
                    view.showError(error2 ?: "Error al extraer texto")
                    return@recognizeTextFromBitmap
                }

                model.saveOcrText(text, fileName, currentGroupId) { textFile, err ->
                    if (err != null || textFile == null) {
                        view.showError(err ?: "Error al guardar OCR")
                    } else {
                        view.showFileSaved(textFile)
                    }
                }
            }
        }
    }

    override fun editTextFileName(fileId: String) {
        view.showEditFileNameDialog(fileId) { newName ->
            // Este ejemplo omite la actualización real en DB
        }
    }

    override fun analyze(imageProxy: ImageProxy) {
        // ML Kit puede analizar en tiempo real, opcional para futuros usos
    }
}
