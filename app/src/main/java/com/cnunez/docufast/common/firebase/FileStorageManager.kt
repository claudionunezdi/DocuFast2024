// FileStorageManager.kt
package com.cnunez.docufast.common.firebase

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FileStorageManager {
    private val storage = Firebase.storage

    /**
     * Sube un archivo a Firebase Storage y devuelve su URL de descarga
     * @param localUri URI local del archivo (content:// o file://)
     * @param remotePath Ruta de destino en Storage (ej: "orgs/org123/groups/group456/images/")
     * @return URL pública de descarga
     */
    suspend fun uploadFile(localUri: String, remotePath: String): String {
        val fileRef = storage.getReference(remotePath)
        val uploadTask = fileRef.putFile(Uri.parse(localUri)).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    /**
     * Obtiene la URL de descarga de un archivo existente
     * @param storagePath Ruta completa en Storage (ej: "orgs/org123/groups/group456/images/file.jpg")
     */
    suspend fun getDownloadUrl(storagePath: String): String? {
        return try {
            storage.getReference(storagePath).downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Elimina un archivo de Storage
     */
    suspend fun deleteFile(storagePath: String): Boolean {
        return try {
            storage.getReference(storagePath).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}