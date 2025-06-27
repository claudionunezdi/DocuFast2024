package com.cnunez.docufast.common.firebase

import android.util.Log
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupDaoRealtime(private val database: FirebaseDatabase) {

    private val ref = database.reference.child("groups")



    // ---- FUNCIÓN PARA CORUTINAS ---- //
    suspend fun deleteGroup(groupId: String) {
        ref.child(groupId).removeValue().await()
    }



    suspend fun createGroupSuspending(group: Group): String {
        val key = ref.push().key ?: throw Exception("No se pudo generar ID")
        val groupWithId = group.copy(id = key)
        ref.child(key).setValue(groupWithId).await()
        return key
    }

    suspend fun getAllGroupsSuspend(): List<Group> {
        val snapshot = ref.get().await()
        return snapshot.children.mapNotNull { it.getValue(Group::class.java) }
    }

    suspend fun getGroupByIdSuspend(groupId: String): Group? =
        ref.child(groupId).get().await().getValue(Group::class.java)

    suspend fun deleteGroupSuspend(groupId: String) {
        ref.child(groupId).removeValue().await()
    }

    suspend fun addFileToGroup(groupId: String, fileId: String) {
        ref.child(groupId).child("files").child(fileId).setValue(true).await()
    }

    suspend fun removeFileFromGroup(groupId: String, fileId: String) {
        ref.child(groupId).child("files").child(fileId).removeValue().await()
    }

    /**
     * Añade un usuario como miembro a un grupo (versión suspendida para corrutinas)
     * @param groupId ID del grupo
     * @param userId ID del usuario a añadir
     */
    suspend fun addMember(groupId: String, userId: String) {
        ref.child(groupId).child("members").child(userId).setValue(true).await()
    }

    /**
     * Elimina un usuario de los miembros de un grupo (versión suspendida)
     * @param groupId ID del grupo
     * @param userId ID del usuario a remover
     */
    suspend fun removeMember(groupId: String, userId: String) {
        ref.child(groupId).child("members").child(userId).removeValue().await()
    }

    /**
     * Obtiene todos los miembros de un grupo (versión suspendida)
     * @param groupId ID del grupo
     * @return Map con los IDs de usuario y estado (true = miembro activo)
     */
    suspend fun getMembers(groupId: String): Map<String, Boolean> = withContext(Dispatchers.IO) {
        ref.child(groupId).child("members").get().await()
            .getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})
            ?: emptyMap()
    }

    // Versión suspendida y segura
    suspend fun insert(group: Group): String {
        val key = ref.push().key ?: throw Exception("Failed to generate group ID")
        ref.child(key).setValue(group.copy(id = key)).await()
        return key
    }

    // Versión para upsert
    suspend fun upsert(group: Group) {
        require(group.id.isNotEmpty()) { "El grupo debe tener un ID válido" }
        ref.child(group.id).setValue(group).await()
    }

    suspend fun createGroup(group: Group): String {
        val key = ref.push().key ?: throw Exception("No se pudo generar ID")
        val groupWithId = group.copy(id = key)
        ref.child(key).setValue(groupWithId).await()
        return key
    }

    suspend fun getAllGroups(): List<Group> {
        val snapshot = ref.get().await()
        return snapshot.children.mapNotNull { it.getValue(Group::class.java) }
    }
}

