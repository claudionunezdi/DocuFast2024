package com.cnunez.docufast.common.firebase.storage

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

object FirebaseStorageManager {
    // Tipos de archivo soportados
    enum class FileType {
        IMAGE, PDF, DOCUMENT, AUDIO, VIDEO;

        companion object {
            fun fromExtension(extension: String): FileType? {
                return when (extension.lowercase()) {
                    "jpg", "jpeg", "png" -> IMAGE
                    "pdf" -> PDF
                    "doc", "docx", "txt" -> DOCUMENT
                    "mp3", "wav", "ogg" -> AUDIO
                    "mp4", "avi", "mkv" -> VIDEO
                    else -> null
                }
            }
        }
    }

    // Tamaños máximos (en bytes)
    const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB
    const val MAX_DOC_SIZE = 20 * 1024 * 1024 // 20MB

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val rtdb = FirebaseDatabase.getInstance().reference

    sealed class UploadResult {
        data class Success(
            val downloadUrl: String,
            val rtdbId: String,
            val fileSize: Long
        ) : UploadResult()

        data class Error(val exception: Exception) : UploadResult()
        data class Progress(val percentage: Double) : UploadResult()
    }

    suspend fun uploadFileSuspend(
        filePath: String,
        fileName: String,
        rtdbPath: String,
        additionalData: Map<String, Any?> = emptyMap(),
        onProgress: (Double) -> Unit = {}
    ): UploadResult {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return UploadResult.Error(IllegalArgumentException("File not found"))
            }

            val sanitizedName = fileName.replace("/", "_")
            val fileRef = storageRef.child("uploads/${System.currentTimeMillis()}_$sanitizedName")

            // Subir archivo con seguimiento de progreso
            val uploadTask = fileRef.putFile(Uri.fromFile(file))
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                onProgress(progress)
            }

            uploadTask.await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            val fileData = createFileData(fileName, downloadUrl, file, additionalData)

            // Guardar metadatos en RTDB
            val newRef = rtdb.child(rtdbPath).push()
            newRef.setValue(fileData).await()

            UploadResult.Success(
                downloadUrl = downloadUrl,
                rtdbId = newRef.key ?: generateFallbackId(),
                fileSize = file.length()
            )
        } catch (e: Exception) {
            UploadResult.Error(e)
        }
    }

    private fun generateFallbackId(): String {
        return "fallback_${System.currentTimeMillis()}"
    }

    private fun createFileData(
        fileName: String,
        url: String,
        file: File,
        additionalData: Map<String, Any?>
    ): Map<String, Any> {
        val baseData = mutableMapOf<String, Any>(
            "name" to fileName,
            "url" to url,
            "uploadedAt" to System.currentTimeMillis(),
            "size" to file.length(),
            "type" to file.extension,
            "mimeType" to getMimeType(file)
        )

        // Filtrar y convertir datos adicionales
        additionalData.forEach { (key, value) ->
            when (value) {
                null -> baseData.remove(key)
                is String, is Number, is Boolean -> baseData[key] = value
                is List<*>, is Map<*, *> -> baseData[key] = value
                else -> baseData[key] = value.toString()
            }
        }

        return baseData
    }

    private fun getMimeType(file: File): String {
        return when (FileType.fromExtension(file.extension)) {
            FileType.IMAGE -> "image/jpeg"
            FileType.PDF -> "application/pdf"
            FileType.DOCUMENT -> when (file.extension) {
                "doc" -> "application/msword"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                else -> "text/plain"
            }
            FileType.AUDIO -> "audio/mpeg"
            FileType.VIDEO -> "video/mp4"
            null -> "application/octet-stream"
        }
    }

    suspend fun deleteFileCompletely(
        storageUrl: String,
        rtdbPath: String
    ): Boolean {
        return try {
            storage.getReferenceFromUrl(storageUrl).delete().await()
            rtdb.child(rtdbPath).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateFileMetadata(
        rtdbPath: String,
        updates: Map<String, Any>
    ): Boolean {
        return try {
            rtdb.child(rtdbPath).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun validateFile(file: File, type: FileType): Boolean {
        if (!file.exists()) return false

        return when {
            type == FileType.IMAGE && file.length() > MAX_IMAGE_SIZE -> false
            type != FileType.IMAGE && file.length() > MAX_DOC_SIZE -> false
            else -> isValidFileType(file, type)
        }
    }

    private fun isValidFileType(file: File, type: FileType): Boolean {
        return FileType.fromExtension(file.extension) == type
    }


    private suspend fun deleteFile(file: File){
        try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    suspend fun getFileMetadata(rtdbPath: String): Map<String, Any>? {
        return try {
            rtdb.child(rtdbPath).get().await().value as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
}