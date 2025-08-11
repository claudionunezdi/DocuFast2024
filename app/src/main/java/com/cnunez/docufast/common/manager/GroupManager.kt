package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.firebase.AppDatabase.groupDao
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.google.firebase.database.FirebaseDatabase
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class GroupManager (private val storageManager: FileStorageManager) {
    private val groupDao = GroupDaoRealtime(FirebaseDatabase.getInstance())
    private val fileDao = FileDaoRealtime(FirebaseDatabase.getInstance(), storageManager)
    private val db = FirebaseDatabase.getInstance()

    // Operaciones básicas (suspensas)
    suspend fun createGroup(group: Group): String = withContext(Dispatchers.IO) {
        groupDao.createGroup(group)
    }

    suspend fun updateGroup(group: Group) = withContext(Dispatchers.IO) {
        groupDao.updateGroup(group)
    }

    // Versión mejorada para asignación de usuarios
    suspend fun assignUserToGroup(groupId: String, userId: String) {
        try {
            withContext(Dispatchers.IO) {
                groupDao.addMemberToGroup(groupId, userId)
            }
        } catch (e: Exception) {
            throw GroupOperationException("Error asignando usuario al grupo", e)
        }
    }

    // Versión con callback para compatibilidad
    fun assignUserToGroup(groupId: String, userId: String, callback: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                groupDao.addMemberToGroup(groupId, userId)
                withContext(Dispatchers.Main) { callback(true, null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(false, e.message) }
            }
        }
    }

    suspend fun removeUserFromGroup(groupId: String, userId: String) = withContext(Dispatchers.IO) {
        groupDao.removeMemberFromGroup(groupId, userId)
    }

    suspend fun getFilesInGroup(groupId: String): List<File> = withContext(Dispatchers.IO) {
        fileDao.getFilesByGroup(groupId).filter { it.metadata.groupId == groupId }
    }

    suspend fun addMemberToGroup(groupId: String, userId: String) {
        val groupRef = FirebaseDatabase.getInstance().getReference("groups/$groupId")

        if (!groupRef.get().await().exists()) {
            throw GroupOperationException("El grupo no existe", null)
        }

        try {
            val membersSnapshot = groupRef.child("members").get().await()
            val currentMembers = membersSnapshot.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()

            val updatedMembers = currentMembers.toMutableMap().apply {
                this[userId] = true
            }

            groupRef.child("members").setValue(updatedMembers).await()
        } catch (e: Exception) {
            throw GroupOperationException("Error añadiendo miembro al grupo", e)
        }
    }

    // Para operaciones que requieran UI thread en el callback
    fun deleteGroup(groupId: String, onComplete: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                groupDao.deleteGroup(groupId)
                withContext(Dispatchers.Main) { onComplete(true, null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false, e.message) }
            }
        }
    }

    // Clase de excepción personalizada
    class GroupOperationException(message: String, cause: Throwable?) : Exception(message, cause)
}