package com.cnunez.docufast.admin.group.detail.model

import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupDetailModel(
    private val groupDao: GroupDaoRealtime,
    private val userDao: UserDaoRealtime,
    private val fileDao: FileDaoRealtime,
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) : GroupDetailContract.Model {
    override suspend fun getGroupById(groupId: String): Group? = withContext(Dispatchers.IO) {
        groupDao.getGroupById(groupId) // Esto ya existe en GroupDaoRealtime
    }

    override suspend fun getGroupMembers(groupId: String): List<User> =
        withContext(Dispatchers.IO) {
            val group = groupDao.getGroupById(groupId) ?: return@withContext emptyList()
            group.members.keys.mapNotNull { userDao.getById(it) }
        }

    override suspend fun isUserAdmin(userId: String): Boolean {
        return userDao.getById(userId)?.role == "ADMIN"
    }


    override suspend fun removeMemberFromGroup(groupId: String, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val updates = hashMapOf<String, Any?>(
                    "/groups/$groupId/members/$userId" to null,
                    "/users/$userId/workGroups/$groupId" to null,
                    "/groupMembers/$groupId/$userId" to null // opcional
                )
                db.reference.updateChildren(updates).await()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    override suspend fun getGroupFiles(groupId: String): List<File> = withContext(Dispatchers.IO) {
        val raw = fileDao.getFilesByGroup(groupId)
        mergeImageAndText(raw)
    }

    override suspend fun deleteGroup(groupId: String) {
        withContext(Dispatchers.IO) {
            groupDao.deleteGroup(groupId) // ignoramos cualquier valor que retorne el DAO
        }
    }

    override suspend fun getOrgUsers(): List<User> = withContext(Dispatchers.IO) {
        userDao.getUsersByCurrentOrganization()
    }

    override suspend fun addUsersToGroup(groupId: String, userIds: List<String>) {
        withContext(Dispatchers.IO) {
            val updates = hashMapOf<String, Any?>()

            // /groups/{groupId}/members/{uid}: true
            userIds.forEach { uid ->
                updates["/groups/$groupId/members/$uid"] = true
                // espejo: /users/{uid}/workGroups/{groupId}: true
                updates["/users/$uid/workGroups/$groupId"] = true
                // opcional espejo secundario:
                updates["/groupMembers/$groupId/$uid"] = true
            }
            db.reference.updateChildren(updates).await()
        }
    }

    private fun mergeImageAndText(files: List<File>): List<File> {
        val byId = files.associateBy { it.id }
        val usados = mutableSetOf<String>()
        val result = mutableListOf<File>()

        // A) Imagen -> Text por linkedOcrTextId
        files.forEach { f ->
            if (f is File.ImageFile && !f.linkedOcrTextId.isNullOrBlank()) {
                val text = byId[f.linkedOcrTextId!!] as? File.TextFile
                if (text != null) {
                    usados += f.id; usados += text.id
                    result += File.OcrResultFile(
                        id = f.id,
                        name = f.name,
                        metadata = f.metadata,
                        storageInfo = f.storageInfo,
                        originalImage = File.OcrResultFile.ImageReference(
                            imageId = f.id,
                            downloadUrl = f.storageInfo.downloadUrl
                        ),
                        extractedText = text.content,
                        confidence = text.ocrData?.confidence ?: 0f
                    )
                }
            }
        }

        // B) Text -> Imagen por sourceImageId
        files.forEach { f ->
            if (f is File.TextFile && !f.sourceImageId.isNullOrBlank() && f.id !in usados) {
                val img = byId[f.sourceImageId!!] as? File.ImageFile
                if (img != null && img.id !in usados) {
                    usados += img.id; usados += f.id
                    result += File.OcrResultFile(
                        id = img.id,
                        name = img.name,
                        metadata = img.metadata,
                        storageInfo = img.storageInfo,
                        originalImage = File.OcrResultFile.ImageReference(
                            imageId = img.id,
                            downloadUrl = img.storageInfo.downloadUrl
                        ),
                        extractedText = f.content,
                        confidence = f.ocrData?.confidence ?: 0f
                    )
                }
            }
        }

        // C) Lo que no se fusionó
        files.forEach { f -> if (f.id !in usados) result += f }
        return result
    }
}



