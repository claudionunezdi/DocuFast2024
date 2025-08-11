package com.cnunez.docufast.camera.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.camera.data.FileRepository
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat

import java.util.Locale
import java.util.UUID

class CameraModel(
    private val context: Context,
    private val fileDao: FileDaoRealtime,
    private val userDao: UserDaoRealtime,
    private val repository: FileRepository = FileRepository(),
    private val io: CoroutineDispatcher = Dispatchers.IO
) : CameraContract.Model {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val _processing = MutableStateFlow(false)

    override fun getProcessingState(): Flow<Boolean> = _processing.asStateFlow()

    // ---------------- OCR ----------------
    override suspend fun recognizeTextFromBitmap(
        bitmap: Bitmap,
        callback: (String?, String?) -> Unit
    ) = withContext(io) {
        _processing.value = true
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            callback(result.text, null)
        } catch (e: Exception) {
            callback(null, "OCR Error: ${e.localizedMessage}")
        } finally {
            _processing.value = false
        }
    }

    // ------------- Subir IMAGEN -------------
    override suspend fun saveImage(
        bitmap: Bitmap,
        fileName: String,
        userId: String,
        metadata: Map<String, Any>
    ): CameraContract.FileReference = withContext(io) {
        _processing.value = true
        try {
            val orgId = metadata["organizationId"] as? String
                ?: throw IllegalArgumentException("organizationId requerido")
            val groupId = metadata["groupId"] as? String
                ?: throw IllegalArgumentException("groupId requerido")

            val userName = userDao.getById(userId)?.name ?: "Usuario desconocido"
            val fileId = UUID.randomUUID().toString()
            val safeName = fileName.sanitizeForStorage()
            val storagePath =
                "organizations/$orgId/groups/$groupId/images/$fileId/${safeName}.jpg"

            // Subir binario a Storage (usa repo -> bucket explícito)
            val (_, downloadUrl) = repository.uploadImage(bitmap, storagePath)

            val imageFile = File.ImageFile(
                id = fileId,
                name = fileName,
                metadata = File.FileMetadata(
                    createdBy = userId,
                    creatorName = userName,
                    groupId = groupId,
                    organizationId = orgId
                ),
                storageInfo = File.StorageInfo(
                    path = storagePath,
                    downloadUrl = downloadUrl
                ),
                dimensions = File.ImageFile.Dimensions(
                    width = bitmap.width,
                    height = bitmap.height,
                    fileSize = getFileSize(bitmap)
                )
            )

            // /files/{id}
            fileDao.insertImage(imageFile)

            // Enlaces mínimos en RTDB
            val db = FirebaseDatabase.getInstance().reference
            val updates = hashMapOf<String, Any?>(
                "/groups/$groupId/files/$fileId" to true,
                "/groups/$groupId/members/$userId" to true
            )
            db.updateChildren(updates).await()

            CameraContract.FileReference(
                id = fileId,
                downloadUrl = downloadUrl,
                filePath = storagePath
            )
        } catch (e: Exception) {
            Log.e("CameraModel", "Error al guardar imagen", e)
            throw e
        } finally {
            _processing.value = false
        }
    }

    // ------------- Subir TEXTO (OCR) -------------
    override suspend fun saveText(
        content: String,
        fileName: String,
        userId: String,
        relatedImageId: String?,
        metadata: Map<String, Any>
    ): Unit = withContext(io) {
        _processing.value = true
        try {
            // 1) Datos requeridos
            val orgIdRaw = metadata["organizationId"] as? String
                ?: throw IllegalArgumentException("organizationId requerido")
            val groupId = metadata["groupId"] as? String
                ?: throw IllegalArgumentException("groupId requerido")

            // 2) Normalizaciones (para Storage SIEMPRE usa org "slug")
            val safeOrg = orgIdRaw.sanitizeForStorage()
            val safeName = fileName.sanitizeForStorage()
            val textId = java.util.UUID.randomUUID().toString()

            // 3) Ruta de Storage (coherente con lo que se guardará en RTDB)
            val storagePath = "organizations/$safeOrg/groups/$groupId/texts/$textId/${safeName}.txt"

            // 4) Subir a Storage (usa tu FileRepository)
            val (_, downloadUrl) = repository.uploadText(content, storagePath)

            // 5) Construir TextFile para RTDB (metadata guarda org "raw", path guarda la ruta slug)
            val userName = userDao.getById(userId)?.name ?: "Usuario desconocido"
            val textFile = File.TextFile(
                id = textId,
                name = fileName,
                metadata = File.FileMetadata(
                    createdBy = userId,
                    creatorName = userName,
                    groupId = groupId,
                    organizationId = orgIdRaw // ← crudo para joins/lecturas
                ),
                storageInfo = File.StorageInfo(
                    path = storagePath,        // ← slug/seguro
                    downloadUrl = downloadUrl
                ),
                content = content,
                sourceImageId = relatedImageId,
                ocrData = File.TextFile.OcrMetadata(
                    confidence = calculateConfidence(content),
                    engine = "ML Kit",
                    processingTimeMs = System.currentTimeMillis()
                ),
                language = "es"
            )

            // 6) Upsert en /files
            fileDao.updateFile(textFile)

            // 7) Enlazar en el grupo
            val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
            val updates = hashMapOf<String, Any?>(
                "/groups/$groupId/files/$textId" to true,
                "/groups/$groupId/members/$userId" to true
            )
            db.updateChildren(updates).await()

            // 8) Si viene de imagen, enlazar imagen -> texto
            relatedImageId?.let { imgId ->
                (fileDao.getFileById(imgId) as? File.ImageFile)?.let { img ->
                    if (img.linkedOcrTextId != textId) {
                        fileDao.updateFile(img.copy(linkedOcrTextId = textId))
                    }
                }
            }

            // <- No retornamos nada (Unit), consistente con el contrato
        } catch (e: Exception) {
            android.util.Log.e("CameraModel", "Error al guardar texto OCR", e)
            throw e
        } finally {
            _processing.value = false
        }
    }

    // ---------------- Helpers ----------------
    private fun String.sanitizeForStorage(): String =
        trim().ifEmpty { dateFormat.format(System.currentTimeMillis()) }
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")

    private fun getFileSize(bitmap: Bitmap): Long {
        val bytes = ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
            it.toByteArray()
        }
        return bytes.size.toLong()
    }

    private fun calculateConfidence(text: String): Float {
        val len = text.length
        return when {
            len >= 800 -> 0.95f
            len >= 200 -> 0.85f
            len >= 50  -> 0.75f
            len > 0    -> 0.6f
            else       -> 0f
        }
    }
}
