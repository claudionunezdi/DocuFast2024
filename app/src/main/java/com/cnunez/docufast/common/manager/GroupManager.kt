package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupManager(
    private val storageManager: FileStorageManager,
    private val groupDao: GroupDaoRealtime = GroupDaoRealtime(FirebaseDatabase.getInstance()),
    private val fileDao: FileDaoRealtime = FileDaoRealtime(FirebaseDatabase.getInstance(), storageManager),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    // ---------------------------
    // CRUD básico de grupos
    // ---------------------------
    suspend fun createGroup(group: Group): String = withContext(Dispatchers.IO) {
        groupDao.createGroup(group) // escribe en /groups/{groupId}
    }

    suspend fun updateGroup(group: Group) = withContext(Dispatchers.IO) {
        groupDao.updateGroup(group) // /groups/{groupId}
    }

    fun deleteGroup(groupId: String, onComplete: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                groupDao.deleteGroup(groupId) // /groups/{groupId}
                withContext(Dispatchers.Main) { onComplete(true, null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false, e.message) }
            }
        }
    }

    // ---------------------------
    // Membresía (siempre 2 vías)
    // ---------------------------
    suspend fun assignUserToGroup(groupId: String, userId: String) = withContext(Dispatchers.IO) {
        val groupRef = db.getReference("groups/$groupId")
        // 1) validación de existencia
        if (!groupRef.get().await().exists()) {
            throw GroupOperationException("El grupo no existe", null)
        }

        // 2) actualizar /groups/{groupId}/members/{userId}: true
        groupRef.child("members").child(userId).setValue(true).await()

        // 3) actualizar /users/{userId}/workGroups/{groupId}: true
        db.getReference("users/$userId/workGroups/$groupId").setValue(true).await()
    }

    fun assignUserToGroup(groupId: String, userId: String, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                assignUserToGroup(groupId, userId)
                withContext(Dispatchers.Main) { callback(true, null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(false, e.message) }
            }
        }
    }

    suspend fun removeUserFromGroup(groupId: String, userId: String) = withContext(Dispatchers.IO) {
        // /groups/{groupId}/members/{userId}: remove
        db.getReference("groups/$groupId/members/$userId").removeValue().await()
        // /users/{userId}/workGroups/{groupId}: remove
        db.getReference("users/$userId/workGroups/$groupId").removeValue().await()
    }

    // ---------------------------
    // Archivos del grupo (RTDB)
    // ---------------------------

    /** Devuelve archivos tal cual están en /files (sin fusionar). */
    suspend fun getFilesInGroupRaw(groupId: String): List<File> = withContext(Dispatchers.IO) {
        // El DAO ya consulta /files con orderByChild("metadata/groupId") == groupId
        fileDao.getFilesByGroup(groupId).filter { it.metadata.groupId == groupId }
    }

    /** Devuelve archivos del grupo, fusionando Image+Text -> OCR_RESULT (1 ítem por captura). */
    suspend fun getFilesInGroupMerged(groupId: String): List<File> = withContext(Dispatchers.IO) {
        val raw = getFilesInGroupRaw(groupId)
        mergeImageAndText(raw)
    }

    // ---------------------------
    // Helpers útiles de grupo
    // ---------------------------

    /** Añade un fileId al mapa /groups/{groupId}/files/{fileId}: true */
    suspend fun addFileToGroup(groupId: String, fileId: String) = withContext(Dispatchers.IO) {
        db.getReference("groups/$groupId/files/$fileId").setValue(true).await()
    }

    /** Quita un fileId del mapa /groups/{groupId}/files/{fileId} */
    suspend fun removeFileFromGroup(groupId: String, fileId: String) = withContext(Dispatchers.IO) {
        db.getReference("groups/$groupId/files/$fileId").removeValue().await()
    }

    /** Obtiene IDs de archivos registrados en el mapa /groups/{groupId}/files */
    suspend fun getGroupFileIds(groupId: String): Set<String> = withContext(Dispatchers.IO) {
        val snap = db.getReference("groups/$groupId/files").get().await()
        val map = snap.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()
        map.filterValues { it }.keys
    }

    // ---------------------------
    // Fusión OCR (igual a la usada en UI)
    // ---------------------------

    private fun mergeImageAndText(files: List<File>): List<File> {
        val byId = files.associateBy { it.id }
        val usados = mutableSetOf<String>()
        val result = mutableListOf<File>()

        // A) Imagen -> Text por linkedOcrTextId
        files.forEach { f ->
            if (f is File.ImageFile && !f.linkedOcrTextId.isNullOrBlank()) {
                val text = byId[f.linkedOcrTextId!!] as? File.TextFile
                if (text != null) {
                    usados += f.id
                    usados += text.id
                    result += toOcrResult(f, text)
                }
            }
        }

        // B) Text -> Imagen por sourceImageId (por si no se fusionó arriba)
        files.forEach { f ->
            if (f is File.TextFile && !f.sourceImageId.isNullOrBlank() && f.id !in usados) {
                val img = byId[f.sourceImageId!!] as? File.ImageFile
                if (img != null && img.id !in usados) {
                    usados += img.id
                    usados += f.id
                    result += toOcrResult(img, f)
                }
            }
        }

        // C) Agregar los que no se fusionaron
        files.forEach { f ->
            if (f.id !in usados) result += f
        }

        return result
    }

    private fun toOcrResult(img: File.ImageFile, txt: File.TextFile): File.OcrResultFile {
        return File.OcrResultFile(
            id = img.id,                       // id del “combo”: el de la imagen
            name = img.name,                   // nombre de la imagen
            metadata = img.metadata,           // org, grupo, creadoPor
            storageInfo = img.storageInfo,     // path/url de la imagen
            originalImage = File.OcrResultFile.ImageReference(
                imageId = img.id,
                downloadUrl = img.storageInfo.downloadUrl
            ),
            extractedText = txt.content,
            confidence = txt.ocrData?.confidence ?: 0f
        )
    }

    suspend fun addUserToGroups(userId: String, groupIds: List<String>) {
        val updates = hashMapOf<String, Any?>()
        groupIds.forEach { gid ->
            updates["/groups/$gid/members/$userId"] = true
            updates["/users/$userId/workGroups/$gid"] = true
            updates["/groupMembers/$gid/$userId"] = true // opcional
        }
        FirebaseDatabase.getInstance().reference.updateChildren(updates).await()
    }

    // ---------------------------
    // Excepción propia
    // ---------------------------
    class GroupOperationException(message: String, cause: Throwable?) : Exception(message, cause)
}
