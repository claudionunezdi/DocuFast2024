package com.cnunez.docufast.admin.user.detail.model

import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserDetailModel(
    private val userDao: UserDaoRealtime = UserDaoRealtime(FirebaseDatabase.getInstance()),
    private val groupDao: GroupDaoRealtime = GroupDaoRealtime(FirebaseDatabase.getInstance()),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) : UserDetailContract.Model {

    override suspend fun fetchUser(userId: String): User? = withContext(Dispatchers.IO) {
        userDao.getById(userId)
    }

    override suspend fun fetchAllGroups(): List<Group> = withContext(Dispatchers.IO) {
        try {
            groupDao.getAllGroups()
        } catch (_: Throwable) {
            val snap = db.getReference("groups").get().await()
            snap.children.mapNotNull { it.getValue(Group::class.java)?.copy(id = it.key ?: "") }
        }
    }

    /**
     * Guarda el usuario en /users/<uid> y sincroniza membresías:
     * - users/<uid>/workGroups
     * - groups/<gid>/members/<uid>
     *
     * IMPORTANTE: no cambia el UID.
     */
    override suspend fun saveUser(user: User) {
        withContext(Dispatchers.IO) {
            require(user.id.isNotBlank()) { "User.id no puede estar vacío" }
            if (user.email.isBlank()) throw IllegalArgumentException("Email no puede estar vacío")

            // Estado anterior para calcular diffs
            val prev = userDao.getById(user.id)
            val oldGroupIds = prev?.workGroups?.keys ?: emptySet()
            val newGroupIds = user.workGroups.keys

            val toAdd = newGroupIds - oldGroupIds
            val toRemove = oldGroupIds - newGroupIds

            val root = db.reference
            val updates = hashMapOf<String, Any?>()

            // 1) Perfil del usuario (upsert)
            updates["users/${user.id}"] = user.toMap().toMutableMap().apply {
                this["role"] = user.role.uppercase()
            }

            // 2) Agregados / Eliminados en grupos
            toAdd.forEach { gid ->
                updates["groups/$gid/members/${user.id}"] = true
                updates["users/${user.id}/workGroups/$gid"] = true
            }
            toRemove.forEach { gid ->
                updates["groups/$gid/members/${user.id}"] = null
                updates["users/${user.id}/workGroups/$gid"] = null
            }

            root.updateChildren(updates).await()
        }
    }

    override suspend fun getGroupById(groupId: String): Group? = withContext(Dispatchers.IO) {
        try {
            groupDao.getGroupById(groupId)
        } catch (_: Throwable) {
            val snap = db.getReference("groups/$groupId").get().await()
            snap.getValue(Group::class.java)?.copy(id = groupId)
        }
    }

    override suspend fun getGroupMembers(groupId: String): List<User> = withContext(Dispatchers.IO) {
        // Lee /groups/<gid>/members (Map<String,Boolean>) y trae los usuarios
        val membersSnap = db.getReference("groups/$groupId/members").get().await()
        val ids: List<String> = membersSnap.children
            .mapNotNull { it.key }
            .filter { membersSnap.child(it).getValue(Boolean::class.java) == true }

        // Traer usuarios (secuencial sencillo; si quieres, puedes paralelizar)
        val result = mutableListOf<User>()
        for (uid in ids) {
            userDao.getById(uid)?.let { result += it }
        }
        result
    }

    override suspend fun isUserAdmin(userId: String): Boolean = withContext(Dispatchers.IO) {
        (userDao.getUserRole(userId) ?: "").equals("ADMIN", ignoreCase = true)
    }

    override suspend fun getGroupFiles(groupId: String): List<File> = withContext(Dispatchers.IO) {
        // Si esta pantalla no los usa, devuelve vacío para cumplir contrato
        emptyList()
    }

    override suspend fun deleteGroup(groupId: String) {
        withContext(Dispatchers.IO) {
            groupDao.deleteGroup(groupId)
        }
    }
}
