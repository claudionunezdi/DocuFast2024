package com.cnunez.docufast.admin.group.detail.model

import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroupDetailModel(
    private val userDao: UserDaoRealtime,
    private val fileDao: FileDaoRealtime,
    private val groupDao: GroupDaoRealtime
) : GroupDetailContract.Model {
    override suspend fun getGroupById(groupId: String): Group? = withContext(Dispatchers.IO) {
        groupDao.getGroupById(groupId) // Esto ya existe en GroupDaoRealtime
    }

    override suspend fun getGroupMembers(groupId: String): List<User> = withContext(Dispatchers.IO) {
        val group = groupDao.getGroupById(groupId) ?: return@withContext emptyList()
        group.members.keys.mapNotNull { userDao.getById(it) }
    }
    override suspend fun isUserAdmin(userId: String): Boolean {
        return userDao.getById(userId)?.role == "ADMIN"
    }


    override suspend fun removeMemberFromGroup(groupId: String, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Obtener el grupo actual
                val group = groupDao.getGroupById(groupId) ?: return@withContext false

                // Crear una nueva versión del grupo sin el usuario
                val updatedGroup = group.removeMember(userId)

                // Actualizar en Firebase
                groupDao.updateGroup(updatedGroup)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun getGroupFiles(groupId: String): List<File> = withContext(Dispatchers.IO) {
        fileDao.getFilesByGroup(groupId) // Usa el método correcto que existe en tu FileDaoRealtime
    }

    override suspend fun deleteGroup(groupId: String) = withContext(Dispatchers.IO) {
        groupDao.deleteGroup(groupId)
    }
}
