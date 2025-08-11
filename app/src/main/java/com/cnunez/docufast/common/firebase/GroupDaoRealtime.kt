package com.cnunez.docufast.common.firebase

import android.util.Log
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupDaoRealtime(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val groupsRef = database.reference.child("groups")
    private val usersRef  = database.reference.child("users")

    // ---------------------------
    // CRUD de grupos
    // ---------------------------

    suspend fun createGroup(group: Group): String = withContext(Dispatchers.IO) {
        val key = if (group.id.isNotBlank()) group.id else groupsRef.push().key
            ?: throw Exception("No se pudo generar ID de grupo")

        val g = group.copy(
            id = key,
            createdAt = if (group.createdAt == 0L) System.currentTimeMillis() else group.createdAt,
            members = group.members.ifEmpty { emptyMap() },
            files = group.files.ifEmpty { emptyMap() }
        )

        val updates = mutableMapOf<String, Any?>(
            "/groups/$key" to g
        )

        // Espejos opcionales: userGroups y groupMembers
        g.members.keys.forEach { uid ->
            updates["/userGroups/$uid/$key"] = true
            updates["/groupMembers/$key/$uid"] = true
            // además, reflect en /users/{uid}/workGroups/{groupId} para que el menú usuario lo vea
            updates["/users/$uid/workGroups/$key"] = true
        }

        database.reference.updateChildren(updates).await()
        key
    }

    suspend fun getGroupById(groupId: String): Group? = withContext(Dispatchers.IO) {
        val snap = groupsRef.child(groupId).get().await()
        if (!snap.exists()) return@withContext null

        // Parse robusto de members/files
        val members: Map<String, Boolean> = when (val mv = snap.child("members").value) {
            is Map<*, *> -> (mv as? Map<String, Boolean>) ?: emptyMap()
            is Boolean -> emptyMap()
            else -> emptyMap()
        }

        val files: Map<String, Boolean> =
            snap.child("files").getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()

        Group(
            id = snap.key ?: "",
            name = snap.child("name").getValue(String::class.java) ?: "",
            description = snap.child("description").getValue(String::class.java) ?: "",
            organization = snap.child("organization").getValue(String::class.java) ?: "",
            members = members,
            files = files,
            createdAt = snap.child("createdAt").getValue(Long::class.java) ?: 0L
        )
    }

    suspend fun getAllGroups(): List<Group> = withContext(Dispatchers.IO) {
        val snap = groupsRef.get().await()
        snap.children.mapNotNull { ds ->
            val members: Map<String, Boolean> = when (val mv = ds.child("members").value) {
                is Map<*, *> -> (mv as? Map<String, Boolean>) ?: emptyMap()
                is Boolean -> emptyMap()
                else -> emptyMap()
            }
            val files: Map<String, Boolean> =
                ds.child("files").getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()

            Group(
                id = ds.key ?: "",
                name = ds.child("name").getValue(String::class.java) ?: "",
                description = ds.child("description").getValue(String::class.java) ?: "",
                organization = ds.child("organization").getValue(String::class.java) ?: "",
                members = members,
                files = files,
                createdAt = ds.child("createdAt").getValue(Long::class.java) ?: 0L
            )
        }
    }

    suspend fun updateGroup(group: Group) = withContext(Dispatchers.IO) {
        require(group.id.isNotBlank()) { "Se requiere ID para actualizar el grupo" }
        groupsRef.child(group.id).setValue(group).await()
    }

    suspend fun deleteGroup(groupId: String) = withContext(Dispatchers.IO) {
        // Limpia espejos y workGroups
        val members = getGroupMembers(groupId).keys
        val updates = mutableMapOf<String, Any?>(
            "/groups/$groupId" to null,
            "/groupMembers/$groupId" to null
        )
        members.forEach { uid ->
            updates["/userGroups/$uid/$groupId"] = null
            updates["/users/$uid/workGroups/$groupId"] = null
        }
        database.reference.updateChildren(updates).await()
    }

    // ---------------------------
    // Membresía (2 vías)
    // ---------------------------

    suspend fun addMemberToGroup(groupId: String, userId: String) = withContext(Dispatchers.IO) {
        val updates = mapOf<String, Any?>(
            "/groups/$groupId/members/$userId" to true,
            "/groupMembers/$groupId/$userId" to true,  // opcional
            "/userGroups/$userId/$groupId" to true,    // opcional
            "/users/$userId/workGroups/$groupId" to true
        )
        database.reference.updateChildren(updates).await()
    }

    suspend fun removeMemberFromGroup(groupId: String, userId: String) = withContext(Dispatchers.IO) {
        val updates = mapOf<String, Any?>(
            "/groups/$groupId/members/$userId" to null,
            "/groupMembers/$groupId/$userId" to null,  // opcional
            "/userGroups/$userId/$groupId" to null,    // opcional
            "/users/$userId/workGroups/$groupId" to null
        )
        database.reference.updateChildren(updates).await()
    }

    suspend fun getGroupMembers(groupId: String): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val snap = groupsRef.child(groupId).child("members").get().await()
        when (val mv = snap.value) {
            is Map<*, *> -> (mv as? Map<String, Boolean>) ?: emptyMap()
            is Boolean -> emptyMap()
            else -> emptyMap()
        }
    }

    // ---------------------------
    // Archivos referenciados
    // ---------------------------

    suspend fun addFileToGroup(groupId: String, fileId: String) = withContext(Dispatchers.IO) {
        groupsRef.child(groupId).child("files").child(fileId).setValue(true).await()
    }

    suspend fun removeFileFromGroup(groupId: String, fileId: String) = withContext(Dispatchers.IO) {
        groupsRef.child(groupId).child("files").child(fileId).removeValue().await()
    }

    suspend fun getGroupFileIds(groupId: String): Set<String> = withContext(Dispatchers.IO) {
        val snap = groupsRef.child(groupId).child("files").get().await()
        val map = snap.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()
        map.filterValues { it }.keys
    }

    // ---------------------------
    // Consultas por organización / usuario
    // ---------------------------

    /** Requiere indexOn: "organization" en /groups (incluido en las rules que te pasé). */
    suspend fun getGroupsByOrganization(organizationId: String): List<Group> = withContext(Dispatchers.IO) {
        try {
            val snapshot = groupsRef.orderByChild("organization").equalTo(organizationId).get().await()
            snapshot.children.mapNotNull { ds ->
                try {
                    val members: Map<String, Boolean> = when (val mv = ds.child("members").value) {
                        is Map<*, *> -> (mv as? Map<String, Boolean>) ?: emptyMap()
                        is Boolean -> emptyMap()
                        else -> emptyMap()
                    }
                    val files: Map<String, Boolean> =
                        ds.child("files").getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()

                    Group(
                        id = ds.key ?: "",
                        name = ds.child("name").getValue(String::class.java) ?: "",
                        description = ds.child("description").getValue(String::class.java) ?: "",
                        organization = ds.child("organization").getValue(String::class.java) ?: "",
                        members = members,
                        files = files,
                        createdAt = ds.child("createdAt").getValue(Long::class.java) ?: 0L
                    ).takeIf { it.name.isNotEmpty() && it.organization == organizationId }
                } catch (e: Exception) {
                    Log.e("GroupDao", "Error parseando grupo ${ds.key}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GroupDao", "Error en getGroupsByOrganization", e)
            emptyList()
        }
    }

    /**
     * Devuelve los grupos a los que pertenece el usuario leyendo /users/{uid}/workGroups
     * y resolviendo en /groups/{groupId}.
     */
    suspend fun getUserGroups(userId: String): List<Group> = withContext(Dispatchers.IO) {
        val wgSnap = usersRef.child(userId).child("workGroups").get().await()
        if (!wgSnap.exists()) return@withContext emptyList()

        val map = wgSnap.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: emptyMap()
        val ids = map.filterValues { it }.keys
        if (ids.isEmpty()) return@withContext emptyList()

        ids.mapNotNull { gid -> getGroupById(gid) }
    }
}
