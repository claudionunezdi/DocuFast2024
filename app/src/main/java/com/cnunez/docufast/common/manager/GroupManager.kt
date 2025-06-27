package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.AppDatabase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupManager(
    private val groupDao: GroupDaoRealtime = GroupDaoRealtime(FirebaseDatabase.getInstance()),
    private val fileDao: FileDaoRealtime = FileDaoRealtime(FirebaseDatabase.getInstance())
) {

    /** Crea un nuevo grupo y devuelve su ID generado */
    suspend fun createGroup(group: Group): String {
        return groupDao.insert(group) // Usará la versión suspendida
    }

    suspend fun updateGroup(group: Group) {
        groupDao.upsert(group)
    }

    suspend fun assignUserToGroup(groupId: String, userId: String) {
        groupDao.addMember(groupId, userId)
    }

    suspend fun removeUserFromGroup(groupId: String, userId: String) {
        groupDao.removeMember(groupId, userId)
    }

    suspend fun getFilesInGroup(groupId: String): List<File> {
        return fileDao.getAll().filter { it.groupId == groupId }
    }


    fun deleteGroup(groupId: String, onComplete: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.groupDao.deleteGroup(groupId)
                withContext(Dispatchers.Main) { onComplete(true, null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false, e.message) }
            }
        }
    }
}
