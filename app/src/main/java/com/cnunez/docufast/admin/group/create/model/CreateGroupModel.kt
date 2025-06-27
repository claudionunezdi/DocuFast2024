package com.cnunez.docufast.admin.group.create.model

import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CreateGroupModel(
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
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val user = userDao.getById(uid)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            if (user.role != "ADMIN") {
                return Result.failure(Exception("No autorizado"))
            }

            val memberMap = members.associate { it.id to true } + (uid to true)

            val group = Group(
                id = "",
                name = name,
                description = description,
                members = memberMap,
                files = emptyMap()
            )

            val groupId = groupDao.insert(group) // Usa la versión suspendida
            Result.success(group.copy(id = groupId)) // Retorna el grupo con ID
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
