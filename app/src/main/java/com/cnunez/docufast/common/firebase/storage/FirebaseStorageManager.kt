package com.cnunez.docufast.common.firebase.storage

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FirebaseStorageManager {

    enum class FileKind { IMAGE, TEXT, DOCUMENT, PDF, AUDIO, VIDEO }

    const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB
    const val MAX_DOC_SIZE   = 20 * 1024 * 1024 // 20MB

    private val storage = FirebaseStorage.getInstance()
    private val rtdb    = FirebaseDatabase.getInstance().reference

    sealed class UploadResult {
        data class Success(val downloadUrl: String, val rtdbId: String?, val fileSize: Long, val storagePath: String): UploadResult()
        data class Error(val exception: Exception) : UploadResult()
        data class Progress(val percentage: Double) : UploadResult()
    }

    /**
     * Compat: sube un archivo local. Si en `additionalData` vienen los metadatos estándar
     * (organizationId, groupId, imageId, fileId, kind, fileName), genera un path coherente:
     *
     *  - kind=IMAGE → organizations/{org}/groups/{group}/images/{fileId}/{fileName}
     *  - kind=TEXT  → orgs/{org}/ocr/{imageId}/{fileId}.txt
     *
     * Si `rtdbPath` comienza con "files/", guarda `additionalData` ahí. En otro caso, no escribe en RTDB.
     */
    suspend fun uploadFileSuspend(
        filePath: String,
        fileName: String,
        rtdbPath: String,
        additionalData: Map<String, Any?> = emptyMap(),
        onProgress: (Double) -> Unit = {}
    ): UploadResult {
        return try {
            val local = File(filePath)
            if (!local.exists()) return UploadResult.Error(IllegalArgumentException("File not found"))

            val org   = (additionalData["organizationId"] as? String).orEmpty()
            val gid   = (additionalData["groupId"] as? String).orEmpty()
            val imgId = (additionalData["imageId"] as? String)
            val fileId= (additionalData["fileId"] as? String)?.ifBlank { null } ?: "f_${System.currentTimeMillis()}"
            val kind  = (additionalData["kind"] as? String)?.uppercase()

            val storagePath = when (kind) {
                "IMAGE" -> FileStorageManager.imagePath(org, gid, fileId, fileName)
                "TEXT"  -> FileStorageManager.ocrTextPath(org, imgId ?: fileId, fileId)
                else    -> "uploads/${System.currentTimeMillis()}_${sanitize(fileName)}" // compat
            }

            val ref   = storage.reference.child(storagePath)
            val task  = ref.putFile(Uri.fromFile(local))
            task.addOnProgressListener { snap ->
                val pct = (100.0 * snap.bytesTransferred) / snap.totalByteCount
                onProgress(pct)
            }
            task.await()

            val url = ref.downloadUrl.await().toString()

            // Solo escribir en RTDB si apunta a /files/...
            var writtenId: String? = null
            if (rtdbPath.startsWith("files/")) {
                val node = rtdb.child(rtdbPath)
                val base = additionalData.toMutableMap().apply {
                    putIfAbsent("name", fileName)
                    put("storageInfo", mapOf("path" to storagePath, "downloadUrl" to url))
                    putIfAbsent("createdAt", System.currentTimeMillis())
                }
                node.updateChildren(base).await()
                writtenId = node.key
            }

            UploadResult.Success(
                downloadUrl = url,
                rtdbId = writtenId,
                fileSize = local.length(),
                storagePath = storagePath
            )
        } catch (e: Exception) {
            UploadResult.Error(e)
        }
    }

    suspend fun deleteFileCompletely(storagePath: String, rtdbPath: String? = null): Boolean {
        return try {
            storage.getReference(storagePath).delete().await()
            if (rtdbPath != null && rtdbPath.startsWith("files/")) {
                rtdb.child(rtdbPath).removeValue().await()
            }
            true
        } catch (_: Exception) { false }
    }

    suspend fun getDownloadUrlFromPath(storagePath: String): String? = try {
        storage.getReference(storagePath).downloadUrl.await().toString()
    } catch (_: Exception) { null }

    private fun sanitize(s: String): String =
        URLEncoder.encode(s, StandardCharsets.UTF_8.toString()).replace("+", "_")
}
