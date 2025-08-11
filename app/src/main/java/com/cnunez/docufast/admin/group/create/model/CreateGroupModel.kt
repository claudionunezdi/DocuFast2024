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
            // si no tienes este método, te dejo su implementación más abajo
            val users = userDao.getByOrganization(org)
            Result.success(users)
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

            if (currentUser.role != "ADMIN") {
                return Result.failure(Exception("Solo administradores pueden crear grupos"))
            }
            if (currentUser.organization.isBlank()) {
                return Result.failure(Exception("El usuario no tiene organización asignada"))
            }

            // todos de la misma org
            if (members.any { it.organization != currentUser.organization }) {
                return Result.failure(Exception("Algunos miembros no pertenecen a la organización"))
            }

            // miembros únicos + admin creador
            val memberIds = (members.map { it.id } + currentUserId)
                .toSet()
                .associateWith { true }

            val group = Group(
                id = "", // lo setea el DAO
                name = name,
                description = description,
                organization = currentUser.organization,
                members = memberIds,
                files = emptyMap(),
                createdAt = System.currentTimeMillis()
            )

            val newId = groupDao.createGroup(group) // <-- este DAO ya actualiza /users/{uid}/workGroups/{groupId}
            Result.success(group.copy(id = newId))
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear el grupo: ${e.message}"))
        }
    }
}