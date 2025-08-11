package com.cnunez.docufast.admin.group.detail.contract

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User

interface GroupDetailContract {
    interface View {
        fun showProgress()
        fun hideProgress()
        fun showGroupName(name: String)
        fun showMembers(users: List<User>)
        fun showFiles(files: List<File>)
        fun onGroupDeleted()
        fun onError(message: String)
        fun setAdminControls(visible: Boolean) // Nuevo método
        fun onMemberRemoved(userId: String) // Nuevo método
        fun showUserPicker(users: List<User>) // para poblar el diálogo
        fun onMembersAddedOk()
        fun showFileLoadingProgress()
        fun hideFileLoadingProgress()
        fun showFileError(message: String)

    }

    interface Presenter {
        fun loadGroupDetails(groupId: String)
        fun deleteGroup(groupId: String)
        fun checkAdminPermissions(userId: String) // Nuevo método
        fun removeMemberFromGroup(groupId: String, userId: String)
        fun loadGroupFiles(groupId: String)
        fun loadAvailableUsersForGroup(groupId: String)
        fun addUsersToGroup(groupId: String, userIds: List<String>)
    }

    interface Model {
        suspend fun getGroupById(groupId: String): Group? // <-
        suspend fun getGroupMembers(groupId: String): List<User>
        suspend fun getGroupFiles(groupId: String): List<File>
        suspend fun deleteGroup(groupId: String)
        suspend fun isUserAdmin(userId: String): Boolean // Nuevo método
        suspend fun removeMemberFromGroup(groupId: String, userId: String): Boolean

        suspend fun getOrgUsers(): List<User>
        suspend fun addUsersToGroup(groupId: String, userIds: List<String>)

    }
}