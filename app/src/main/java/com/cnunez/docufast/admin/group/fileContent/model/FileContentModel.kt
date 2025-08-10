package com.cnunez.docufast.admin.group.fileContent.model

import com.cnunez.docufast.admin.group.fileContent.contract.FileContentContract
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.ImageFile
import com.cnunez.docufast.common.dataclass.File.TextFile
import com.cnunez.docufast.common.dataclass.File.OcrResultFile
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FileContentModel(
    private val fileDao: FileDaoRealtime,
    private val userDao: UserDaoRealtime
) : FileContentContract.Model {

    override suspend fun getFileById(id: String): File? = withContext(Dispatchers.IO) {
        try {
            fileDao.getFileById(id)?.let { file ->
                // Obtener nombre del creador
                val creatorName = userDao.getById(file.metadata.createdBy)?.name ?: ""

                // Actualizar metadata con el nombre
                when (file) {
                    is TextFile -> file.copy(
                        metadata = file.metadata.copy(creatorName = creatorName)
                    )
                    is ImageFile -> file.copy(
                        metadata = file.metadata.copy(creatorName = creatorName)
                    )
                    is OcrResultFile -> file.copy(
                        metadata = file.metadata.copy(creatorName = creatorName)
                    )
                    else -> file
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // Actualizar metadatos de modificación
            val updatedFile = when (file) {
                is TextFile -> file.copy(
                    metadata = file.metadata.copy(
                        lastModified = currentFormattedDate()
                    )
                )
                is OcrResultFile -> file.copy(
                    metadata = file.metadata.copy(
                        lastModified = currentFormattedDate()
                    )
                )
                else -> return@withContext false
            }

            fileDao.updateFile(updatedFile)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener el archivo para determinar el grupo
            val file = fileDao.getFileById(fileId) ?: return@withContext false

            // 2. Eliminar el archivo (incluye Storage y Realtime DB)
            fileDao.deleteFile(fileId, file.metadata.groupId)

            // 3. Si es un OCR, opcionalmente eliminar la imagen fuente
            if (file is OcrResultFile && file.originalImage.imageId.isNotEmpty()) {
                // Opcional: Descomentar para eliminar también la imagen fuente
                // fileDao.deleteFile(file.originalImage.imageId, file.metadata.groupId)
            }

            true
        } catch (e: Exception) {
            false
        }
    }


    private fun currentFormattedDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    companion object {
        private const val TAG = "FileContentModel"
    }
}