package com.cnunez.docufast.admin.group.create.model

import android.util.Log
import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class CreateGroupModel @Inject constructor(
    private val userDao: UserDaoRealtime,
    private val groupDao: GroupDaoRealtime
) : CreateGroupContract.Model {

    override suspend fun getUsersByOrganization(org: String): Result<List<User>> {
        return try {
            val allUsers = userDao.getAll()
            val filtered = allUsers.filter { it.organization == org }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveGroup(
        name: String,
        description: String,
        members: List<User>
    ): Result<Group> {
        return try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val currentUser = userDao.getById(currentUserId)
                ?: return Result.failure(Exception("Usuario actual no encontrado"))

            // Validación de rol y organización
            if (currentUser.role != "ADMIN") {
                return Result.failure(Exception("Solo administradores pueden crear grupos"))
            }

            if (currentUser.organization.isNullOrEmpty()) {
                return Result.failure(Exception("El usuario no tiene organización asignada"))
            }

            // Crear mapa de miembros incluyendo al creador
            val memberMap = members.associate { it.id to true } + (currentUserId to true)

            val group = Group(
                id = "", // Generado por Firebase
                name = name,
                description = description,
                organization = currentUser.organization,
                members = members.associate { it.id to true } + (currentUserId to true),
                files = emptyMap(), // Asegurar que files existe
                createdAt = System.currentTimeMillis()
            )

            val newGroupId = groupDao.createGroup(group)
            Log.d("CreateGroup", "Nuevo grupo creado con ID: $newGroupId")
            Result.success(group.copy(id = newGroupId))
        } catch (e: Exception) {
            Log.e("CreateGroup", "Error al crear grupo", e)
            Result.failure(Exception("Error al crear el grupo: ${e.message}"))
        }
    }
}