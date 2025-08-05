package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.FileType
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import  com.cnunez.docufast.common.firebase.storage.FirebaseStorageManager

class FileDaoRealtime(
    private val db: FirebaseDatabase,
    private val storageManager: FileStorageManager
) {
    private val filesRef = db.getReference("files")

    // -------------------- Operaciones CRUD mejoradas --------------------
    suspend fun uploadFile(file: File, localUri: String): String = withContext(Dispatchers.IO) {
        try {
            // 1. Subir archivo a Storage
            val storagePath = "orgs/${file.organizationId}/groups/${file.groupId}/${file.getFileType()}/${file.name}"
            val downloadUrl = storageManager.uploadFile(localUri, storagePath)

            // 2. Actualizar metadatos
            file.apply {
                this.storagePath = storagePath
                this.downloadUrl = downloadUrl
                id = filesRef.push().key ?: throw Exception("Error generando ID")
            }

            // 3. Guardar en Realtime DB
            filesRef.child(file.id).setValue(file.toMap()).await()

            // 4. Actualizar referencia en grupo
            db.getReference("groups/${file.groupId}/files/${file.id}").setValue(true)

            file.id
        } catch (e: Exception) {
            throw Exception("Error subiendo archivo: ${e.message}")
        }
    }

    suspend fun getById(id: String): File? = withContext(Dispatchers.IO) {
        filesRef.child(id).get().await().let { snapshot ->
            File.fromSnapshot(snapshot)
        }
    }

    suspend fun deleteFile(fileId: String, groupId: String) = withContext(Dispatchers.IO) {
        // 1. Eliminar de Storage (opcional, podría hacerse via Cloud Function)
        getById(fileId)?.let { file ->
            storageManager.deleteFile(file.storagePath)
        }

        // 2. Eliminar de Realtime DB
        filesRef.child(fileId).removeValue().await()

        // 3. Eliminar referencia en grupo
        db.getReference("groups/$groupId/files/$fileId").removeValue().await()
    }

    // -------------------- Consultas organizacionales --------------------
    suspend fun getFilesByOrganization(orgId: String): List<File> {
        return filesRef.orderByChild("organizationId")
            .equalTo(orgId)
            .get()
            .await()
            .children
            .mapNotNull { File.fromSnapshot(it) }
    }

    suspend fun getFilesByGroup(groupId: String): List<File> {
        return filesRef.orderByChild("groupId")
            .equalTo(groupId)
            .get()
            .await()
            .children
            .mapNotNull { File.fromSnapshot(it) }
    }

    suspend fun getFilesByType(groupId: String, type: FileType): List<File> {
        return filesRef.orderByChild("groupId")
            .equalTo(groupId)
            .get()
            .await()
            .children
            .mapNotNull { snapshot ->
                File.fromSnapshot(snapshot)?.takeIf { it.getFileType() == type }
            }
    }

    // -------------------- Helper Methods --------------------
    suspend fun getFileDownloadUrl(fileId: String): String? {
        return getById(fileId)?.downloadUrl ?: run {
            val file = getById(fileId) ?: return null
            storageManager.getDownloadUrl(file.storagePath).also { url ->
                // Actualizar en DB si la URL estaba vacía
                if (file.downloadUrl.isEmpty()) {
                    filesRef.child(fileId).child("downloadUrl").setValue(url)
                }
            }
        }
    }
}