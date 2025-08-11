package com.cnunez.docufast.camera.presenter

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.cnunez.docufast.camera.contract.CameraContract
import com.google.firebase.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class CameraPresenter(
    private val view: CameraContract.View,
    @get:VisibleForTesting internal val model: CameraContract.Model,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())
) : CameraContract.Presenter {

    // Estado interno
    private val isProcessing = AtomicBoolean(false)
    private var lastCapturedBitmap: Bitmap? = null
    private var lastOcrResult: String? = null
    private var currentContext: Context? = null

    private data class Context(
        val userId: String,
        val groupId: String,
        val organizationId: String,
        val customMetadata: Map<String, Any> = emptyMap()
    )

    // Inicialización: Observa el estado de procesamiento
    init {
        observeProcessingState()
    }

    private fun observeProcessingState() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                model.getProcessingState()
                    .distinctUntilChanged()
                    .collect { isProcessing ->
                        view.showLoading(isProcessing)
                    }
            } catch (e: Exception) {
                Log.e("CameraPresenter", "Error observing state", e)
                view.showLoading(false)
            }
        }
    }

    override fun setGroupContext(
        groupId: String,
        organizationId: String,
        userId: String,
        metadata: Map<String, Any>?
    ) {
        currentContext = Context(
            userId = userId,
            groupId = groupId,
            organizationId = organizationId,
            customMetadata = metadata ?: emptyMap()
        )
        observeProcessingState() // Reinicia la observación con el nuevo userId
    }

    override suspend fun applyOcr(bitmap: Bitmap) {
        if (!validateContext()) return
        if (isProcessing.getAndSet(true)) {
            view.showError("Operación en progreso")
            return
        }

        try {
            model.recognizeTextFromBitmap(bitmap) { text, error ->
                isProcessing.set(false)
                when {
                    error != null -> view.showError("Error en OCR: ${error.take(100)}")
                    text.isNullOrEmpty() -> view.showError("No se detectó texto")
                    else -> {
                        lastCapturedBitmap = bitmap
                        lastOcrResult = text
                        view.showOcrResult(text)
                        view.enableSaveOptions()
                    }
                }
            }
        } catch (e: Exception) {
            isProcessing.set(false)
            view.showError("Error en OCR: ${e.message}")
        }
    }

    override suspend fun saveContent(
        fileName: String,
        type: CameraContract.SaveType
    ): Result<Unit> {
        if (!validateContext()) {
            return Result.failure(Exception("Contexto no configurado"))
        }

        val context = currentContext ?: return Result.failure(Exception("Contexto inválido"))

        return try {
            val result = when (type) {
                CameraContract.SaveType.IMAGE_ONLY -> saveImageOnly(fileName, context)
                CameraContract.SaveType.OCR_RESULT -> saveWithOcr(fileName, context)
            }

            if (result.isSuccess) {

                view.onFileUploaded(context.groupId)
            }

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveImageOnly(
        fileName: String,
        context: Context
    ): Result<Unit> {
        val bitmap = lastCapturedBitmap ?: return Result.failure(Exception("No hay imagen para guardar"))

        return try {
            model.saveImage(
                bitmap = bitmap,
                fileName = fileName,
                userId = context.userId,
                metadata = buildMetadata(context)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar imagen: ${e.message}"))
        }
    }

    private suspend fun saveWithOcr(
        fileName: String,
        context: Context
    ): Result<Unit> {
        val text = lastOcrResult ?: return Result.failure(Exception("No hay texto OCR"))
        val bitmap = lastCapturedBitmap ?: return Result.failure(Exception("No hay imagen asociada"))

        return try {
            val imageRef = model.saveImage(
                bitmap = bitmap,
                fileName = "IMG_$fileName",
                userId = context.userId,
                metadata = buildMetadata(context)
            )

            model.saveText(
                content = text,
                fileName = fileName,
                userId = context.userId,
                relatedImageId = imageRef.id,
                metadata = buildMetadata(context)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar OCR: ${e.message}"))
        }
    }

    private fun buildMetadata(context: Context): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            put("userId", context.userId)
            put("groupId", context.groupId)
            put("organizationId", context.organizationId)
            putAll(context.customMetadata)
        }
    }

    private fun validateContext(): Boolean {
        if (currentContext?.userId.isNullOrEmpty()) {
            view.showError("Usuario no configurado")
            return false
        }
        return true
    }

    override fun getLastCapturedImage(): Bitmap? = lastCapturedBitmap
    override fun getLastOcrText(): String? = lastOcrResult

    fun onDestroy() {
        coroutineScope.cancel()
    }

    @VisibleForTesting
    internal fun getCurrentState() = Pair(lastCapturedBitmap, lastOcrResult)
}