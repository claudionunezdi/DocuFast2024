package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.UserDaoRealtime

class UserManager(private val userDao: UserDaoRealtime) {

    suspend fun getAllUsers(): List<User> =
        userDao.getAll()

    suspend fun getUserById(id: String): User? =
        userDao.getById(id)

    suspend fun createUser(user: User): String =
        userDao.insert(user)

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun deleteUser(id: String) {
        userDao.delete(id)
    }
}
