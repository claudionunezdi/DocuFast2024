// UserListModel.kt
package com.cnunez.docufast.admin.user.list.model

import android.util.Log
import com.cnunez.docufast.admin.user.list.contract.UserListContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserListModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userDao: UserDaoRealtime = UserDaoRealtime(FirebaseDatabase.getInstance())
) : UserListContract.Model {

    override suspend fun fetchUsers(): List<User> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")
            val myProfile = userDao.getById(currentUser.uid) ?: throw Exception("Perfil no encontrado")

            if (!myProfile.isAdmin()) {
                throw Exception("Solo administradores pueden ver usuarios")
            }

            userDao.getByOrganization(myProfile.organization).also {
                Log.d("UserListModel", "Usuarios obtenidos: ${it.size}")
            }
        } catch (e: Exception) {
            Log.e("UserListModel", "Error fetching users", e)
            throw e
        }
    }

    override suspend fun deleteUser(userId: String) {
        try {
            userDao.delete(userId)
        } catch (e: Exception) {
            Log.e("UserListModel", "Error deleting user", e)
            throw e
        }
    }
}
