package com.cnunez.docufast.camera.presenter

import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


import kotlinx.coroutines.withContext

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraPresenter(
    private val view: CameraContract.View,
    @get:VisibleForTesting internal val model: CameraContract.Model
) : CameraContract.Presenter {

    // Contexto del grupo/organización
    private lateinit var currentGroupId: String
    private lateinit var currentOrganizationId: String

    // Estado de las operaciones
    private val isProcessing = AtomicBoolean(false)
    private var lastCapturedBitmap: Bitmap? = null
    private var lastImageFile: ImageFile? = null

    /* Manejo del contexto */
    override fun setGroupContext(groupId: String, organizationId: String) {
        this.currentGroupId = groupId
        this.currentOrganizationId = organizationId
    }

    /* Procesamiento de OCR */
    override fun applyOcr(bitmap: Bitmap) {
        if (isProcessing.getAndSet(true)) {
            view.showError("Another operation is in progress")
            return
        }

        model.recognizeTextFromBitmap(bitmap) { text, error ->
            isProcessing.set(false)

            when {
                error != null -> view.showError("OCR Error: $error")
                text.isNullOrEmpty() -> view.showError("No text detected")
                else -> {
                    lastCapturedBitmap = bitmap
                    view.showOcrResult(text)
                }
            }
        }
    }

    /* Guardado de archivos */
    override fun saveOcrText(fileName: String) {
        if (isProcessing.getAndSet(true)) {
            view.showError("Save already in progress")
            return
        }

        val bitmap = lastCapturedBitmap ?: run {
            isProcessing.set(false)
            view.showError("No captured image available")
            return
        }

        // Unified save process
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: Save image
                val imageFile = saveImageToStorage(bitmap)

                // Step 2: Get OCR text
                val text = getOcrText() ?: throw Exception("No text available")

                // Step 3: Save text
                saveTextFile(fileName, text, imageFile.id)

                withContext(Dispatchers.Main) {
                    view.showImageSaved(imageFile)
                    view.showFileSaved(TextFile(
                        id = "",
                        imageFileId = imageFile.id,
                        content = text,
                        fileName = fileName,
                        created = System.currentTimeMillis(),
                        organizationId = currentOrganizationId,
                        groupId = currentGroupId,
                        timestamp = System.currentTimeMillis()
                    ))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { view.showError(e.message ?: "Save failed") }
            } finally {
                isProcessing.set(false)
            }
        }
    }

    private suspend fun saveImageToStorage(bitmap: Bitmap): ImageFile {
        return suspendCoroutine { continuation ->
            model.saveImageToStorage(bitmap, currentGroupId) { imageFile, error ->
                if (error != null || imageFile == null) {
                    continuation.resumeWithException(Exception(error ?: "Image save failed"))
                } else {
                    continuation.resume(imageFile)
                }
            }
        }
    }

    private suspend fun getOcrText(): String? {
        return suspendCoroutine { continuation ->
            lastCapturedBitmap?.let { bitmap ->
                model.recognizeTextFromBitmap(bitmap) { text, error ->
                    if (error != null) {
                        continuation.resume(null)
                    } else {
                        continuation.resume(text)
                    }
                }
            } ?: continuation.resume(null)
        }
    }

    private suspend fun saveTextFile(
        fileName: String,
        text: String,
        imageFileId: String
    ) {
        return suspendCoroutine { continuation ->
            model.saveOcrText(
                text = text,
                fileName = fileName,
                groupId = currentGroupId,
                organizationId = currentOrganizationId,
                imageFileId = imageFileId
            ) { textFile, error ->
                if (error != null || textFile == null) {
                    continuation.resumeWithException(Exception(error ?: "Failed to save text"))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    @VisibleForTesting
    override fun handleTextRecognitionResult(
        text: String?,
        error: String?,
        fileName: String
    ) {
        when {
            error != null -> {
                isProcessing.set(false)
                view.showError("OCR Error: $error")
            }
            text.isNullOrEmpty() -> {
                isProcessing.set(false)
                view.showError("No text to save")
            }
            else -> {
                // Paso 3: Guardar texto en Database
                model.saveOcrText(
                    text = text,
                    fileName = fileName,
                    groupId = currentGroupId,
                    organizationId = currentOrganizationId,
                    imageFileId = lastImageFile?.id ?: ""
                ) { textFile, saveError ->
                    isProcessing.set(false)

                    if (saveError != null || textFile == null) {
                        view.showError(saveError ?: "Failed to save text")
                    } else {
                        view.showFileSaved(textFile)
                    }
                }
            }
        }
    }

    /* Estado actual */
    fun hasPendingOperations(): Boolean = isProcessing.get()

    @VisibleForTesting
    internal fun getLastImageFile(): ImageFile? = lastImageFile
}