package com.cnunez.docufast.camera.contract

import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile

/**
 * Contrato que define la comunicación entre View-Presenter-Model
 * para el módulo de cámara y reconocimiento de texto.
 */
interface CameraContract {

    /**
     * Interfaz para la Vista (Activity/Fragment).
     * Maneja la UI y eventos del usuario.
     */
    interface View {
        /** Muestra errores al usuario */
        fun showError(message: String)

        /** Muestra la imagen capturada */
        fun showPhoto(bitmap: Bitmap)

        /** Muestra el resultado del OCR */
        fun showOcrResult(text: String)

        /** Confirma que el archivo de texto fue guardado */
        fun showFileSaved(textFile: TextFile)

        /** Confirma que la imagen fue guardada */
        fun showImageSaved(imageFile: ImageFile)

        /** Verifica si hay texto OCR disponible */
        fun isOcrResultAvailable(): Boolean

        /** Obtiene el último resultado de OCR (para evitar reprocesamiento) */
        fun getOcrResult(): String?
    }

    /**
     * Interfaz para el Presenter.
     * Contiene la lógica de negocio y coordina Model-View.
     */
    interface Presenter {
        /** Establece el contexto grupal/organizacional */
        fun setGroupContext(groupId: String, organizationId: String)

        /** Procesa una imagen para extraer texto (OCR) */
        fun applyOcr(bitmap: Bitmap)

        /** Guarda el texto reconocido en la base de datos */
        fun saveOcrText(fileName: String)

        /** Versión extendida para testing */
        @VisibleForTesting
        fun handleTextRecognitionResult(text: String?, error: String?, fileName: String)
    }

    /**
     * Interfaz para el Modelo.
     * Maneja datos y operaciones complejas.
     */
    interface Model {
        /** Extrae texto de una imagen usando ML Kit */
        fun recognizeTextFromBitmap(
            bitmap: Bitmap,
            callback: (String?, String?) -> Unit
        )

        /** Guarda la imagen en Firebase Storage */
        fun saveImageToStorage(
            bitmap: Bitmap,
            groupId: String,
            onResult: (ImageFile?, String?) -> Unit
        )

        /** Persiste el texto reconocido en Realtime Database */
        fun saveOcrText(
            text: String,
            fileName: String,
            groupId: String,
            organizationId: String,
            imageFileId: String,
            onResult: (TextFile?, String?) -> Unit
        )
    }

    companion object {
        // Códigos de error estandarizados
        const val ERROR_OCR_FAILED = "OCR_FAILED"
        const val ERROR_INVALID_IMAGE = "INVALID_IMAGE"
        const val ERROR_MISSING_CONTEXT = "MISSING_GROUP_OR_ORG"
    }
}