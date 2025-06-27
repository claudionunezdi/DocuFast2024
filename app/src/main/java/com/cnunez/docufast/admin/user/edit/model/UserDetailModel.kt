// UserDetailModel.kt
package com.cnunez.docufast.admin.user.edit.model

import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserDetailModel(
    private val userDao: UserDaoRealtime = UserDaoRealtime(FirebaseDatabase.getInstance()),
    private val groupDao: GroupDaoRealtime = GroupDaoRealtime(FirebaseDatabase.getInstance())
) : UserDetailContract.Model {

    override suspend fun fetchUser(userId: String): User? =
        withContext(Dispatchers.IO) {
            userDao.getById(userId)
        }

    override suspend fun fetchAllGroups(): List<Group> =
        withContext(Dispatchers.IO) {
            groupDao.getAllGroupsSuspend()
        }

    // Ahora retorna Unit y espera al await() de la actualización
    override suspend fun saveUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.update(user)        // ← correcto: update() es suspend
        }
    }
}
