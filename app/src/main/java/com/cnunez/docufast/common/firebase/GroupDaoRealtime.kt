package com.cnunez.docufast.common.firebase

import android.util.Log
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupDaoRealtime(private val database: FirebaseDatabase = FirebaseDatabase.getInstance()) {
    private val groupsRef = database.reference.child("groups")
    private  val db = FirebaseDatabase.getInstance()

    suspend fun createGroup(group: Group): String {
        val key = groupsRef.push().key ?: throw Exception("Couldn't generate group ID")
        val g = group.copy(id = key)

        val updates = mutableMapOf<String, Any?>()
        updates["/groups/$key"] = g

        // Índice por usuario (y espejo opcional)
        g.members.keys.forEach { uid ->
            updates["/userGroups/$uid/$key"] = true
            updates["/groupMembers/$key/$uid"] = true // opcional si lo usas
        }

        database.reference.updateChildren(updates).await()
        return key
    }

    suspend fun getGroupById(groupId: String): Group? {
        return groupsRef.child(groupId).get().await().getValue(Group::class.java)
    }

    suspend fun getAllGroups(): List<Group> {
        val snap = groupsRef.get().await()
        return snap.children.mapNotNull { ds ->
            ds.getValue(Group::class.java)?.apply { id = ds.key ?: "" }
        }
    }

    suspend fun updateGroup(group: Group) {
        require(group.id.isNotEmpty()) { "Group ID required for update" }
        groupsRef.child(group.id).setValue(group).await()
    }

    suspend fun deleteGroup(groupId: String) {
        // 1) Leer miembros para limpiar índices
        val members = getGroupMembers(groupId).keys

        // 2) Multi-update: borrar grupo + índices
        val updates = mutableMapOf<String, Any?>(
            "/groups/$groupId" to null,
            "/groupMembers/$groupId" to null // opcional
        )
        members.forEach { uid ->
            updates["/userGroups/$uid/$groupId"] = null
        }
        database.reference.updateChildren(updates).await()
    }

    suspend fun addMemberToGroup(groupId: String, userId: String) {
        val updates = mapOf(
            "/groups/$groupId/members/$userId" to true,
            "/userGroups/$userId/$groupId" to true,
            "/groupMembers/$groupId/$userId" to true // opcional
        )
        database.reference.updateChildren(updates).await()
    }

    suspend fun removeMemberFromGroup(groupId: String, userId: String) {
        val updates = mapOf<String, Any?>(
            "/groups/$groupId/members/$userId" to null,
            "/userGroups/$userId/$groupId" to null,
            "/groupMembers/$groupId/$userId" to null // opcional
        )
        database.reference.updateChildren(updates).await()
    }
    suspend fun getGroupMembers(groupId: String): Map<String, Boolean> {
        return withContext(Dispatchers.IO) {
            groupsRef.child(groupId).child("members").get().await()
                .getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})
                ?: emptyMap()
        }
    }
    suspend fun getAllGroupsWithMembers(): List<Group> {
        return groupsRef.get().await().children.mapNotNull { snapshot ->
            snapshot.getValue(Group::class.java)?.apply {
                id = snapshot.key ?: ""
                // Asegura que members se cargue correctamente
                members = snapshot.child("members").getValue(object: GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()
            }
        }
    }
    suspend fun addFileToGroup(groupId: String, fileId: String) {
        groupsRef.child(groupId).child("files").child(fileId).setValue(true).await()
    }
    suspend fun removeFileFromGroup(groupId: String, fileId: String) {
        groupsRef.child(groupId).child("files").child(fileId).removeValue().await()
    }
    // Ya tienes este método implementado, solo asegúrate de usarlo
    suspend fun getGroupsByOrganization(organizationId: String): List<Group> {
        return try {
            val snapshot = groupsRef.orderByChild("organization")
                .equalTo(organizationId)
                .get()
                .await()

            snapshot.children.mapNotNull { ds ->
                try {
                    // Manejo especial para 'members'
                    val members = when (val membersValue = ds.child("members").value) {
                        is Map<*, *> -> membersValue as? Map<String, Boolean> ?: emptyMap()
                        is Boolean -> emptyMap() // Si es boolean, lo tratamos como vacío
                        else -> emptyMap()
                    }

                    Group(
                        id = ds.key ?: "",
                        name = ds.child("name").getValue(String::class.java) ?: "",
                        description = ds.child("description").getValue(String::class.java) ?: "",
                        organization = ds.child("organization").getValue(String::class.java) ?: "",
                        members = members,
                        files = ds.child("files").getValue(object: GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap(),
                        createdAt = ds.child("createdAt").getValue(Long::class.java) ?: 0L
                    ).takeIf { it.name.isNotEmpty() && it.organization == organizationId }
                } catch (e: Exception) {
                    Log.e("GroupDao", "Error parsing group ${ds.key}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GroupDao", "Error in getGroupsByOrganization", e)
            emptyList()
        }
    }


    suspend fun getGroupsForCurrentUser(userId: String): List<Group> {
        val currentOrg = SessionManager.getCurrentUser()?.organization ?: return emptyList()
        return getGroupsByOrganization(currentOrg) // si de verdad filtras por org
    }







}