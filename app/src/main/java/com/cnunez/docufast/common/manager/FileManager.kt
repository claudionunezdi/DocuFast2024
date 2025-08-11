package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileManager(
    private val fileDao: FileDaoRealtime,
    private val userDao: UserDaoRealtime
) {

    // Wrapper con metadatos “humanos”
    data class FileWithMeta(
        val file: File,              // Puede ser OCR_RESULT (fusionado)
        val creatorName: String,     // Nombre legible
        val creatorEmail: String?    // Email si existe
    )

    /**
     * Carga archivos de un grupo y los entrega:
     * 1) fusionados (Image+Text => OCR_RESULT) cuando corresponda
     * 2) enriquecidos con nombre/email del creador
     */
    suspend fun getFilesWithMetadata(groupId: String): List<FileWithMeta> = withContext(Dispatchers.IO) {
        // 1) Trae desde /files por metadata/groupId (como en el seed)
        val raw = fileDao.getFilesByGroup(groupId)

        // 2) Fusiona imagen+texto a OCR_RESULT (para mostrar 1 solo ítem por captura)
        val merged = mergeImageAndText(raw)

        // 3) Enriquecer con datos del creador (si metadata.creatorName viene vacío)
        val creatorIds = merged.map { it.metadata.createdBy }.toSet()
        val creators = creatorIds.associateWith { uid -> userDao.getById(uid) }

        return@withContext merged.map { f ->
            val fallbackName = "Usuario ${f.metadata.createdBy.take(6)}"
            val creatorName = f.metadata.creatorName
                .takeIf { it.isNotBlank() }
                ?: creators[f.metadata.createdBy]?.name?.takeIf { it.isNotBlank() }
                ?: fallbackName

            FileWithMeta(
                file = f,
                creatorName = creatorName,
                creatorEmail = creators[f.metadata.createdBy]?.email
            )
        }
    }

    /**
     * Obtiene un archivo puntual (por id). Si es parte de un par OCR, intenta devolver
     * la versión fusionada (OCR_RESULT). También lo enriquece con datos del creador.
     */
    suspend fun getFileWithMetadata(fileId: String): FileWithMeta? = withContext(Dispatchers.IO) {
        val base = fileDao.getFileById(fileId) ?: return@withContext null

        val fused = when (base) {
            is File.ImageFile -> {
                val text = base.linkedOcrTextId
                    ?.let { fileDao.getFileById(it) as? File.TextFile }
                if (text != null) toOcrResult(base, text) else base
            }
            is File.TextFile -> {
                val img = base.sourceImageId
                    ?.let { fileDao.getFileById(it) as? File.ImageFile }
                if (img != null) toOcrResult(img, base) else base
            }
            else -> base
        }

        val user = userDao.getById(fused.metadata.createdBy)
        val name = fused.metadata.creatorName
            .takeIf { it.isNotBlank() }
            ?: user?.name?.takeIf { it.isNotBlank() }
            ?: "Usuario ${fused.metadata.createdBy.take(6)}"

        return@withContext FileWithMeta(
            file = fused,
            creatorName = name,
            creatorEmail = user?.email
        )
    }

    // -------------------------
    // Helpers de fusión OCR
    // -------------------------

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

        // C) Los que no se fusionaron quedan tal cual
        files.forEach { f ->
            if (f.id !in usados) result += f
        }

        return result
    }

    private fun toOcrResult(img: File.ImageFile, txt: File.TextFile): File.OcrResultFile {
        return File.OcrResultFile(
            id = img.id,                       // usamos el id de la imagen como id del “combo”
            name = img.name,                   // y el nombre de la imagen
            metadata = img.metadata,           // metadata de la imagen (org, grupo, creador)
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
