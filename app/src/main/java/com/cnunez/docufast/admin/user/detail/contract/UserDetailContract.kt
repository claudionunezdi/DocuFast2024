package com.cnunez.docufast.admin.user.detail.contract

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group

interface UserDetailContract {
    interface View {
        fun showProgress()
        fun hideProgress()
        fun displayUser(user: User)
        fun displayGroups(groups: List<Group>, selectedIds: Set<String>)
        fun onUpdateSuccess()
        fun onError(message: String)
    }

    interface Presenter {
        fun loadUser(userId: String)
        fun updateUser(name: String, email: String?, selectedGroupIds: List<String>, newPassword: String?)
        fun clear()
    }

    interface Model {
        suspend fun fetchUser(userId: String): User?
        suspend fun fetchAllGroups(): List<Group>
        suspend fun saveUser(user: User)
        suspend fun getGroupById(groupId: String): Group? // <-- Añadir este método
        suspend fun getGroupMembers(groupId: String): List<User>
        suspend fun isUserAdmin(userId: String): Boolean
        suspend fun getGroupFiles(groupId: String): List<File>
        suspend fun deleteGroup(groupId: String)


    }
}