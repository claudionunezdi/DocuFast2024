package com.cnunez.docufast.camera.contract

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * Contrato que define la comunicación entre los componentes del módulo de cámara
 * siguiendo el patrón MVP (Model-View-Presenter) con influencia de Clean Architecture.
 */
interface CameraContract {

    // 1. MODELO DE DATOS PARA REFERENCIAS DE ARCHIVOS ------------------
    /**
     * Representa una referencia a un archivo subido a Firebase.
     * @property id Identificador único en Firebase
     * @property downloadUrl URL pública de descarga
     * @property filePath Ruta completa en Storage (opcional)
     */
    data class FileReference(
        val id: String,
        val downloadUrl: String,
        val filePath: String = ""
    )

    // 2. TIPOS DE OPERACIONES ------------------------------------------
    /**
     * Define los modos de guardado disponibles:
     * - IMAGE_ONLY: Guarda solo la imagen capturada
     * - OCR_RESULT: Guarda la imagen + texto reconocido (relacionados)
     */
    enum class SaveType {
        IMAGE_ONLY,
        OCR_RESULT
    }

    // 3. INTERFAZ DE LA VISTA (VIEW) -----------------------------------
    /**
     * Contrato para la interfaz de usuario (Activity/Fragment).
     * Maneja:
     * - Mostrar errores/éxitos
     * - Actualizar la UI según estados
     * - Interacción con el usuario
     */
    interface View {
        /**
         * Muestra un mensaje de error al usuario.
         * @param message Mensaje legible para el usuario
         */
        fun showError(message: String)

        /**
         * Muestra el resultado del OCR procesado.
         * @param text Texto reconocido (puede ser procesado)
         */
        fun showOcrResult(text: String)

        /**
         * Muestra confirmación de éxito en una operación.
         * @param message Mensaje descriptivo
         */
        fun showSuccess(message: String)

        /**
         * Actualiza el estado de carga/espera.
         * @param isLoading true para mostrar indicador de carga
         */
        fun showLoading(isLoading: Boolean)

        /**
         * Habilita/deshabilita opciones de guardado.
         */
        fun enableSaveOptions()
    }

    // 4. INTERFAZ DEL MODELO (MODEL) -----------------------------------
    /**
     * Contrato para el manejo de datos y operaciones complejas:
     * - Acceso a Firebase (Storage/Realtime DB)
     * - Procesamiento con ML Kit
     * - Operaciones de IO
     */
    interface Model {


        /**
         * Procesa una imagen para extraer texto usando ML Kit OCR.
         * @param bitmap Imagen a procesar
         * @param callback Función que recibe (texto, error)
         */
        suspend fun recognizeTextFromBitmap(
            bitmap: Bitmap,
            callback: (String?, String?) -> Unit
        )

        /**
         * Guarda una imagen en Firebase Storage y registra metadatos.
         * @param bitmap Imagen a guardar
         * @param fileName Nombre descriptivo
         * @param userId ID del usuario que sube el archivo
         * @param metadata Metadatos adicionales (grupo, organización, etc.)
         * @return FileReference con datos del archivo guardado
         */
        suspend fun saveImage(
            bitmap: Bitmap,
            fileName: String,
            userId: String,
            metadata: Map<String, Any>
        ): FileReference

        /**
         * Guarda texto reconocido relacionado con una imagen.
         * @param content Texto a guardar
         * @param fileName Nombre descriptivo
         * @param userId ID del usuario
         * @param relatedImageId ID de la imagen relacionada (opcional)
         * @param metadata Metadatos adicionales
         */
        suspend fun saveText(
            content: String,
            fileName: String,
            userId: String,
            relatedImageId: String?,
            metadata: Map<String, Any>
        )

        /**
         * Flujo para observar el estado de procesamiento.
         */
        fun getProcessingState(): Flow<Boolean>
    }

    // 5. INTERFAZ DEL PRESENTADOR (PRESENTER) --------------------------
    /**
     * Coordinador entre View y Model:
     * - Maneja lógica de negocio
     * - Gestiona el ciclo de vida
     * - Administra el estado
     */
    interface Presenter {
        /**
         * Configura el contexto necesario para las operaciones.
         * @param userId ID del usuario actual (requerido)
         * @param groupId ID del grupo (opcional)
         * @param organizationId ID de la organización (opcional)
         * @param customMetadata Metadatos personalizados (opcional)
         */
        fun setGroupContext(
            groupId: String,
            organizationId: String,
            userId: String,
            metadata: Map<String, Any>? = null // Añade este parámetro
        )

        /**
         * Procesa una imagen para extraer texto (OCR).
         * @param bitmap Imagen a procesar
         */
        suspend fun applyOcr(bitmap: Bitmap)

        /**
         * Guarda el contenido según el tipo especificado.
         * @param fileName Nombre base para los archivos
         * @param type Tipo de guardado (IMAGE_ONLY u OCR_RESULT)
         * @return Resultado de la operación
         */
        suspend fun saveContent(
            fileName: String,
            type: SaveType
        ): Result<Unit>

        /**
         * Obtiene la última imagen capturada (para previsualización).
         */
        fun getLastCapturedImage(): Bitmap?

        /**
         * Obtiene el último texto reconocido (para edición/confirmación).
         */
        fun getLastOcrText(): String?
    }
}