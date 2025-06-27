package com.cnunez.docufast.common.manager

import android.os.Build
import androidx.annotation.RequiresApi
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.PhotoDaoFirebase
import com.cnunez.docufast.common.firebase.TextFileDaoFirebase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant

class ArchivesManager(
    private val photoDao: PhotoDaoFirebase,
    private val textDao: TextFileDaoFirebase,
    private val fileDao: FileDaoRealtime,
    private val groupDao: GroupDaoRealtime
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createArchive(
        imageFile: ImageFile,
        ocrText: String
    ): String {
        // 1) Guarda la imagen
        val imgId = photoDao.insert(imageFile)

        // 2) Genera un timestamp legible para el nombre de archivo
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "OCR_${timestamp}.txt"

        // 3) Crea y guarda el TextFile
        val textFile = TextFile(
            imageFileId = imgId,
            content     = ocrText,
            fileName    = fileName,
            created     = System.currentTimeMillis(),
            groupId     = imageFile.groupId
        )
        val txtId = textDao.insert(textFile)

        // 4) Genera la fecha de creación en ISO8601
        val creationDate = try {
            Instant.now().toString()
        } catch (e: NoClassDefFoundError) {
            // fallback para minSdk < 26
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        }

        // 5) Arma y guarda el File completo
        val file = File(
            name         = fileName,
            creationDate = creationDate,
            createdBy    = imageFile.createdBy,
            groupId      = imageFile.groupId,
            imageFile    = imageFile.apply { id = imgId },
            textFile     = textFile.apply { id = txtId },

        )
        val fileId = fileDao.insert(file)

        // 6) Asocia al grupo
        groupDao.addFileToGroup(imageFile.groupId, fileId)
        return fileId


    }

    suspend fun fetchArchives(groupId: String): List<File> =
        fileDao.getAll().filter { it.groupId == groupId }





    suspend fun assignUserToGroup(groupId: String, userId: String) =
        groupDao.addMember(groupId, userId)

    suspend fun removeUserFromGroup(groupId: String, userId: String) =
        groupDao.removeMember(groupId, userId)

    suspend fun deleteArchiveSuspend(archiveId: String, groupId: String) {
        groupDao.removeFileFromGroup(groupId, archiveId)
    }



}
