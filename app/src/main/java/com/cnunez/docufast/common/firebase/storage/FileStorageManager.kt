package com.cnunez.docufast.common.firebase.storage

import android.net.Uri
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.ImageFile
import com.cnunez.docufast.common.dataclass.File.TextFile
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Singleton

@Singleton
class FileStorageManager {

    private val storage = Firebase.storage.apply {
        maxUploadRetryTimeMillis = 10_000L
    }

    companion object {
        @Volatile private var instance: FileStorageManager? = null
        fun getInstance(): FileStorageManager =
            instance ?: synchronized(this) {
                instance ?: FileStorageManager().also { instance = it }
            }

        // Helpers de path estándar usados en toda la app:
        fun imagePath(orgId: String, groupId: String, imageId: String, fileName: String): String {
            val safeName = sanitize(fileName.ifBlank { "$imageId.jpg" })
            return "organizations/${sanitize(orgId)}/groups/${sanitize(groupId)}/images/${sanitize(imageId)}/$safeName"
        }

        fun ocrTextPath(orgId: String, imageId: String, textId: String): String {
            return "orgs/${sanitize(orgId)}/ocr/${sanitize(imageId)}/${sanitize(textId)}.txt"
        }

        fun ocrExtractedPath(orgId: String, imageId: String): String {
            return "orgs/${sanitize(orgId)}/ocr/${sanitize(imageId)}/extracted.txt"
        }

        private fun sanitize(s: String): String =
            URLEncoder.encode(s, StandardCharsets.UTF_8.toString()).replace("+", "_")
    }

    // -------------------- Operaciones básicas --------------------

    suspend fun uploadFile(fileUri: Uri, storagePath: String): String = withContext(Dispatchers.IO) {
        val ref = storage.getReference(storagePath)
        ref.putFile(fileUri).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun uploadBytes(bytes: ByteArray, storagePath: String): Uri = withContext(Dispatchers.IO) {
        val ref = storage.getReference(storagePath)
        ref.putBytes(bytes).await()
        ref.downloadUrl.await()
    }

    suspend fun getDownloadUrl(storagePath: String): String = withContext(Dispatchers.IO) {
        storage.getReference(storagePath).downloadUrl.await().toString()
    }

    suspend fun deleteFile(storagePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            storage.getReference(storagePath).delete().await()
            true
        } catch (_: Exception) { false }
    }

    // -------------------- Operaciones tipadas (coherentes con /files) --------------------

    /**
     * Genera un path coherente si el File no trae uno, sube el archivo y retorna el File actualizado
     * con `storageInfo.path` y `storageInfo.downloadUrl`.
     */
    suspend fun uploadTypedFile(file: File, localUri: Uri): File = withContext(Dispatchers.IO) {
        val computedPath = when (file) {
            is ImageFile -> {
                val org = file.metadata.organizationId.ifBlank { "default_org" }
                val gid = file.metadata.groupId.ifBlank { "default_group" }
                val id  = file.id.ifBlank { "no_id" }
                val name = file.name.ifBlank { "$id.jpg" }
                imagePath(org, gid, id, name)
            }
            is TextFile -> {
                val org = file.metadata.organizationId.ifBlank { "default_org" }
                val img = file.sourceImageId ?: file.id
                val txt = file.id.ifBlank { "text_${System.currentTimeMillis()}" }
                ocrTextPath(org, img, txt)
            }
            else -> throw IllegalArgumentException("Unsupported file type: ${file::class.java.simpleName}")
        }

        val finalPath = file.storageInfo.path.ifBlank { computedPath }
        val url = uploadFile(localUri, finalPath)

        return@withContext when (file) {
            is ImageFile -> file.copy(storageInfo = file.storageInfo.copy(path = finalPath, downloadUrl = url))
            is TextFile  -> file.copy(storageInfo = file.storageInfo.copy(path = finalPath, downloadUrl = url))
            else -> file
        }
    }
}
