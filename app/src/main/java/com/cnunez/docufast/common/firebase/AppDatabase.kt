package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppDatabase {

    // Firebase RTDB
    val firebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    // Storage manager (singleton)
    val storageManager: FileStorageManager by lazy { FileStorageManager.getInstance() }

    // DAOs alineados al seed (/files y /groups)
    val fileDao: FileDaoRealtime by lazy {
        FileDaoRealtime(
            db = firebaseDatabase,
            storageManager = storageManager
        )
    }

    val groupDao: GroupDaoRealtime by lazy {
        GroupDaoRealtime(firebaseDatabase)
    }

    // ===== Helpers de alto nivel =====

    // Archivos del usuario autenticado (/files con index metadata/createdBy)
    suspend fun getCurrentUserFiles(): List<File> = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")
        fileDao.getFilesByUserId(userId)
    }

    suspend fun getCurrentUserFilesSafe(): Result<List<File>> = try {
        Result.success(getCurrentUserFiles())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Grupos del usuario (usa /users/{uid}/workGroups -> /groups/{id})
    suspend fun getCurrentUserGroups(): List<Group> = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext emptyList()
        groupDao.getUserGroups(userId)  // <- corregido (antes llamabas a getGroupsForCurrentUser)
    }

    // Grupos por organización (requiere indexOn: "organization" en /groups)
    suspend fun getOrganizationGroups(organizationId: String): List<Group> {
        return groupDao.getGroupsByOrganization(organizationId)
    }

    // Archivos del grupo (fusionados Imagen+Texto -> OCR_RESULT para mostrar 1 ítem por captura)
    suspend fun getGroupFilesMerged(groupId: String): List<File> = withContext(Dispatchers.IO) {
        val raw = fileDao.getFilesByGroup(groupId) // /files?orderByChild=metadata/groupId&equalTo=groupId
        mergeImageAndText(raw)
    }

    // ====== Merged helpers (misma lógica que usamos en otras capas) ======
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
            id = img.id,
            name = img.name,
            metadata = img.metadata,           // org, groupId, createdBy
            storageInfo = img.storageInfo,     // path/url de la imagen
            originalImage = File.OcrResultFile.ImageReference(
                imageId = img.id,
                downloadUrl = img.storageInfo.downloadUrl
            ),
            extractedText = txt.content,
            confidence = txt.ocrData?.confidence ?: 0f
        )
    }
}
