package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime

class FileManager(
    private val fileDao: FileDaoRealtime,
    private val userDao: UserDaoRealtime
) {
    // Clase wrapper que combina File con metadatos extendidos
    data class FileWithMeta(
        val file: File,
        val creatorName: String,
        val creatorEmail: String? = null
    )

    suspend fun getFilesWithMetadata(groupId: String): List<FileWithMeta> {
        val files = fileDao.getFilesByGroup(groupId)
        return files.mapNotNull { file ->
            val creator = userDao.getById(file.metadata.createdBy)
            creator?.let {
                FileWithMeta(
                    file = file,
                    creatorName = creator.name.takeIf { it.isNotEmpty() } ?: "Usuario ${creator.id.take(6)}",
                    creatorEmail = creator.email
                )
            }
        }
    }

    // Otras operaciones del FileManager...
    suspend fun getFileWithMetadata(fileId: String): FileWithMeta? {
        val file = fileDao.getFileById(fileId) ?: return null
        val creator = userDao.getById(file.metadata.createdBy) ?: return null

        return FileWithMeta(
            file = file,
            creatorName = creator.name.takeIf { it.isNotEmpty() } ?: "Usuario ${creator.id.take(6)}",
            creatorEmail = creator.email
        )
    }
}