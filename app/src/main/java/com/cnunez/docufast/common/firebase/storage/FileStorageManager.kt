package com.cnunez.docufast.common.firebase.storage

import android.net.Uri
import com.cnunez.docufast.common.dataclass.File
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton
import com.cnunez.docufast.common.dataclass.File.ImageFile
import com.cnunez.docufast.common.dataclass.File.TextFile
import java.io.File as LocalFile

@Singleton
class FileStorageManager {
    private val storage = Firebase.storage.apply {
        maxUploadRetryTimeMillis = 10_000 // 10 segundos para operaciones
    }

    companion object {
        @Volatile private var instance: FileStorageManager? = null

        fun getInstance(): FileStorageManager {
            return instance ?: synchronized(this) {
                instance ?: FileStorageManager().also { instance = it }
            }
        }
    }

    // -------------------- Operaciones Básicas --------------------
    suspend fun uploadFile(fileUri: Uri, storagePath: String): String {
        return try {
            val fileRef = storage.getReference(storagePath)
            fileRef.putFile(fileUri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw StorageException("Error uploading file to $storagePath", e)
        }
    }

    suspend fun downloadFileToLocal(storagePath: String, localFile: LocalFile): Long {
        return storage.getReference(storagePath)
            .getFile(localFile)
            .await()
            .totalByteCount
    }

    suspend fun deleteFile(storagePath: String): Boolean {
        return try {
            storage.getReference(storagePath).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getDownloadUrl(storagePath: String): String {
        return storage.getReference(storagePath)
            .downloadUrl
            .await()
            .toString()
    }

    // -------------------- Operaciones Tipadas --------------------
    suspend fun uploadTypedFile(file: File, localUri: Uri): File {
        val storagePath = generateStoragePath(file)
        val downloadUrl = uploadFile(localUri, storagePath)

        return when (file) {
            is ImageFile -> file.copy(
                storageInfo = file.storageInfo.copy(
                    path = storagePath,
                    downloadUrl = downloadUrl
                )
            )
            is TextFile -> file.copy(
                storageInfo = file.storageInfo.copy(
                    path = storagePath,
                    downloadUrl = downloadUrl
                )
            )
            else -> throw IllegalArgumentException("Unsupported file type")
        }
    }

    fun generateStoragePath(file: File): String {
        return when (file) {
            is ImageFile -> "orgs/${file.metadata.organizationId}/images/${file.id}"
            is TextFile -> "orgs/${file.metadata.organizationId}/texts/${file.id}"
            else -> throw IllegalArgumentException("Unsupported file type")
        }
    }

    // -------------------- Clase de Excepción --------------------
    class StorageException(message: String, cause: Throwable?) : Exception(message, cause)
}