// UserListModel.kt
package com.cnunez.docufast.admin.user.list.model

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
        // Sólo ADMIN puede listar usuarios de su organización
        val uid = auth.currentUser?.uid
            ?: throw Exception("Usuario no autenticado")
        val me = userDao.getById(uid)
            ?: throw Exception("Perfil no encontrado")
        if (me.role != "ADMIN") throw Exception("Permisos insuficientes")
        // Filtra por organización
        return userDao.getAll().filter { it.organization == me.organization }
    }

    override suspend fun deleteUser(userId: String) {
        userDao.delete(userId)
    }
}
