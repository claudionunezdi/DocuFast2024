package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.UserDaoRealtime

class UserManager(private val userDao: UserDaoRealtime) {
    suspend fun getAll(): List<User> = userDao.getAll()
    suspend fun getById(id: String): User? = userDao.getById(id)
    suspend fun create(user: User): String = userDao.insert(user)
    suspend fun update(user: User) = userDao.update(user)
    suspend fun delete(id: String) = userDao.delete(id)
}