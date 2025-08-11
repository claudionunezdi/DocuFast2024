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

    override suspend fun getOrgUsers(): List<User> = withContext(Dispatchers.IO) {
        userDao.getUsersByCurrentOrganization()
    }

    override suspend fun addUsersToGroup(groupId: String, userIds: List<String>) {
        withContext(Dispatchers.IO) {
            val groupRef = db.getReference("groups/$groupId")

            // Normaliza members (por si hay datos legacy como Boolean)
            val membersSnap = groupRef.child("members").get().await()
            val current: MutableMap<String, Boolean> = try {
                membersSnap.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})?.toMutableMap()
                    ?: mutableMapOf()
            } catch (_: Exception) {
                mutableMapOf()
            }

            userIds.forEach { current[it] = true }
            groupRef.child("members").setValue(current).await()
        }
    }
}


