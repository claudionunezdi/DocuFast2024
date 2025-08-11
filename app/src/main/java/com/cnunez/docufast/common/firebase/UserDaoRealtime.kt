package com.cnunez.docufast.common.firebase

import android.util.Log
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class UserDaoRealtime(private val db: FirebaseDatabase) {
     val usersRef = db.getReference("users")

    // -------------------- CRUD Básico --------------------
    suspend fun upsertById(user: User): String = withContext(Dispatchers.IO) {
        require(user.id.isNotBlank()) { "User.id no puede estar vacío" }
        usersRef.child(user.id)
            .setValue(user.copy(role = user.role.uppercase()))
            .await()
        user.id
    }

    suspend fun updateName(userId: String, newName: String) = withContext(Dispatchers.IO) {
        require(userId.isNotBlank()) { "User.id requerido" }
        usersRef.child(userId).child("name").setValue(newName).await()
    }

    suspend fun delete(userId: String) = withContext(Dispatchers.IO) {
        usersRef.child(userId).removeValue().await()
    }

    suspend fun getById(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            Log.d("UserDao", "Fetching user with ID: $userId")
            val snapshot = usersRef.child(userId).get().await()
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)?.apply {
                    id = snapshot.key ?: userId
                }
                Log.d("UserDao", "User found: ${user?.name}")
                user
            } else {
                Log.d("UserDao", "User not found")
                null
            }
        } catch (e: Exception) {
            Log.e("UserDao", "Error fetching user", e)
            null
        }
    }

    // -------------------- Funciones Especiales --------------------
    suspend fun getUserRole(userId: String): String? = withContext(Dispatchers.IO) {
        usersRef.child(userId).child("role").get().await().getValue(String::class.java)
    }

    suspend fun updateUserRole(adminId: String, targetUserId: String, newRole: String) {
        require(newRole in listOf("ADMIN", "USER")) { "Rol inválido" }
        val adminRole = getUserRole(adminId) ?: throw Exception("Admin no encontrado")
        if (adminRole != "ADMIN") throw SecurityException("Requiere rol ADMIN")

        usersRef.child(targetUserId).child("role").setValue(newRole).await()
    }
    suspend fun getAll(): List<User> = withContext(Dispatchers.IO) {
        usersRef.get().await().children.mapNotNull { it.toUser() }

    }



    // -------------------- Extensión Interna --------------------
    private fun DataSnapshot.toUser(): User? {
        return getValue(User::class.java)?.copy(id = key ?: return null)
    }

    suspend fun getUsersByCurrentOrganization(): List<User> = withContext(Dispatchers.IO) {
        val currentOrg = SessionManager.getCurrentOrganization() ?: return@withContext emptyList()
        usersRef.orderByChild("organization").equalTo(currentOrg).get().await()
            .children.mapNotNull { it.toUser() }
    }


    suspend fun getFilesByCurrentOrganization(): List<File> {
        val currentOrg = SessionManager.getCurrentOrganization() ?:
        return emptyList()


        return try {
            val snapshot = db.getReference("files").orderByChild("organizationId").equalTo(currentOrg).get().await()
            snapshot.children.mapNotNull { it.getValue(File::class.java) }
        } catch (e: Exception) {
            Log.e("UserDao", "Error obteniendo archivos por organización", e)
            emptyList()
        }
    }

    suspend fun getUsersByOrganization(organizationId: String): List<User> {
        return usersRef.orderByChild("organization")
            .equalTo(organizationId)
            .get()
            .await()
            .children
            .mapNotNull { it.toUser() }
    }

    suspend fun addUserToOrganization(userId: String, orgId: String) {
        usersRef.child(userId).child("organization").setValue(orgId).await()
    }

    suspend fun promoteToAdmin(adminId: String, targetUserId: String) {
        val admin = getById(adminId) ?: throw Exception("Admin no encontrado")
        if (!admin.isAdmin()) throw SecurityException("Requiere rol ADMIN")

        usersRef.child(targetUserId).child("role").setValue("ADMIN").await()
    }

    suspend fun getByOrganization(organizationId: String): List<User> {
        return try {
            Log.d("UserDao", "Buscando usuarios para org: $organizationId")
            usersRef.orderByChild("organization")
                .equalTo(organizationId)
                .get()
                .await()
                .children
                .mapNotNull {
                    it.getValue(User::class.java)?.apply {
                        id = it.key ?: ""
                    }.also {
                        Log.d("UserDao", "Usuario encontrado: ${it?.name}")
                    }
                }
                .also {
                    Log.d("UserDao", "Total usuarios encontrados: ${it.size}")
                }
        } catch (e: Exception) {
            Log.e("UserDao", "Error al obtener usuarios por org", e)
            emptyList()
        }
    }



    suspend fun syncUserGroups(userId: String) {
        val userGroupsRef = db.getReference("users/$userId/workGroups")
        val groupsRef = db.getReference("groups")

        // 1. Buscar grupos donde el usuario es miembro
        val snapshot = groupsRef.orderByChild("members/$userId").equalTo(true).get().await()

        // 2. Actualizar workGroups del usuario
        val updates = HashMap<String, Any>()
        snapshot.children.forEach {
            updates["users/$userId/workGroups/${it.key}"] = true
        }

        db.reference.updateChildren(updates).await()
    }





}