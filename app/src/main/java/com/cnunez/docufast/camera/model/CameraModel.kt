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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CameraModel(
    private val context: Context,
    private val fileDao: FileDaoRealtime,
    private val userDao: UserDaoRealtime,  // <-- Tercer parámetro
    private val repository: FileRepository = FileRepository()
) : CameraContract.Model {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    override suspend fun recognizeTextFromBitmap(
        bitmap: Bitmap,
        callback: (String?, String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            callback(result.text, null)
        } catch (e: Exception) {
            callback(null, "OCR Error: ${e.localizedMessage}")
        }
    }
    override suspend fun saveImage(
        bitmap: Bitmap,
        fileName: String,
        userId: String,
        metadata: Map<String, Any>
    ): CameraContract.FileReference {
        val orgId = metadata["organizationId"] as? String ?: throw IllegalArgumentException("organizationId requerido")
        val groupId = metadata["groupId"] as? String ?: throw IllegalArgumentException("groupId requerido")

        val userName = userDao.getById(userId)?.name ?: "Usuario desconocido"
        val fileId = UUID.randomUUID().toString()
        val storagePath = "organizations/$orgId/groups/$groupId/images/$fileId/${fileName.sanitizeForStorage()}.jpg"

        return try {
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

            fileDao.insertImage(imageFile)

            val db = FirebaseDatabase.getInstance()

            // 🔹 Vincular en /groups/{groupId}/files
            db.getReference("groups/$groupId/files/$fileId").setValue(true)

            // 🔹 Asegurar que el usuario esté en /members
            db.getReference("groups/$groupId/members/$userId").setValue(true)

            CameraContract.FileReference(fileId, downloadUrl, storagePath)
        } catch (e: Exception) {
            Log.e("CameraModel", "Error al guardar imagen", e)
            throw e
        }
    }

    override suspend fun saveText(
        content: String,
        fileName: String,
        userId: String,
        relatedImageId: String?,
        metadata: Map<String, Any>
    ) {
        val orgId = metadata["organizationId"] as? String ?: throw IllegalArgumentException("organizationId requerido")
        val groupId = metadata["groupId"] as? String ?: throw IllegalArgumentException("groupId requerido")

        val userName = userDao.getById(userId)?.name ?: "Usuario desconocido"
        val fileId = UUID.randomUUID().toString()
        val storagePath = "organizations/$orgId/groups/$groupId/texts/$fileId/${fileName.sanitizeForStorage()}.txt"

        try {
            val (_, downloadUrl) = repository.uploadText(content, storagePath)

            val textFile = File.TextFile(
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
                content = content,
                sourceImageId = relatedImageId,
                ocrData = File.TextFile.OcrMetadata(
                    confidence = calculateConfidence(content),
                    processingTimeMs = System.currentTimeMillis()
                ),
                language = "es"
            )

            fileDao.updateFile(textFile)

            val db = FirebaseDatabase.getInstance()

            // 🔹 Vincular en /groups/{groupId}/files
            db.getReference("groups/$groupId/files/$fileId").setValue(true)

            // 🔹 Asegurar que el usuario esté en /members
            db.getReference("groups/$groupId/members/$userId").setValue(true)

            // 🔹 Enlazar texto con la imagen OCR si existe
            relatedImageId?.let { imageId ->
                fileDao.getFileById(imageId)?.let { file ->
                    if (file is File.ImageFile) {
                        fileDao.updateFile(file.copy(linkedOcrTextId = fileId))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CameraModel", "Error al guardar texto", e)
            throw e
        }
    }


    override fun getProcessingState(): Flow<Boolean> {
        // Implementación básica - ajusta según tu lógica
        return flow {
            // Simulamos estado de procesamiento
            emit(false) // Estado inicial
            delay(1000)
            emit(true) // Procesando
            delay(2000)
            emit(false) // Finalizado
        }.flowOn(Dispatchers.IO)

        /* O si usas Firebase directamente:
        return callbackFlow {
            val ref = Firebase.database.getReference("processingState")
            val listener = ref.addValueEventListener { snapshot ->
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }
            awaitClose { ref.removeEventListener(listener) }
        }*/
    }


    // Helpers
    private fun getFileSize(bitmap: Bitmap): Long {
        return ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, this)
        }.toByteArray().size.toLong()
    }

    private fun calculateConfidence(text: String): Float {
        return when {
            text.isEmpty() -> 0f
            else -> {
                val validChars = text.count { it.isLetterOrDigit() || it.isWhitespace() }
                (validChars.toFloat() / text.length * 100).coerceIn(0f, 100f)
            }
        }
    }

    private fun String.sanitizeForStorage(): String {
        return this.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    }
}