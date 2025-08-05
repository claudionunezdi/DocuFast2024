// UserDetailModel.kt
package com.cnunez.docufast.admin.user.detail.model

import android.util.Log
import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserDetailModel(
    private val userDao: UserDaoRealtime = UserDaoRealtime(FirebaseDatabase.getInstance()),
    private val groupDao: GroupDaoRealtime = GroupDaoRealtime(FirebaseDatabase.getInstance())
) : UserDetailContract.Model {

    override suspend fun fetchUser(userId: String): User? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("UserDetailModel", "Fetching user with ID: $userId")
                val user = userDao.getById(userId)
                Log.d("UserDetailModel", "User fetched: ${user != null}")
                user
            } catch (e: Exception) {
                Log.e("UserDetailModel", "Error fetching user", e)
                null
            }
        }

    override suspend fun fetchAllGroups(): List<Group> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("UserDetailModel", "Fetching all groups")
                val groups = groupDao.getAllGroups()
                Log.d("UserDetailModel", "Groups fetched: ${groups.size}")
                groups
            } catch (e: Exception) {
                Log.e("UserDetailModel", "Error fetching groups", e)
                emptyList()
            }
        }

    override suspend fun saveUser(user: User) {
        withContext(Dispatchers.IO) {
            try {
                // Validar que el email no esté vacío
                if (user.email.isBlank()) throw IllegalArgumentException("Email no puede estar vacío")

                Log.d("UserDetailModel", "Saving user: ${user.id}")
                userDao.update(user)
                Log.d("UserDetailModel", "User saved successfully")
            } catch (e: Exception) {
                Log.e("UserDetailModel", "Error saving user", e)
                throw e
            }
        }
    }
    override suspend fun getGroupById(groupId: String): Group? = withContext(Dispatchers.IO) {
        groupDao.getGroupById(groupId) // Esto ya existe en GroupDaoRealtime
    }

    override suspend fun getGroupMembers(groupId: String): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun isUserAdmin(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupFiles(groupId: String): List<File> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroup(groupId: String) {
        TODO("Not yet implemented")
    }
}
