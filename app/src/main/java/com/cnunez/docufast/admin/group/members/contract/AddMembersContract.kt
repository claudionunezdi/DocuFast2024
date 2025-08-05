package com.cnunez.docufast.admin.group.members.contract

import com.cnunez.docufast.common.dataclass.User

interface AddMembersContract {
    interface View {
        fun showUsers(users: List<User>)
        fun onMembersAdded()
        fun showError(message: String)
    }

    interface Presenter {
        fun loadAvailableUsers(organizationId: String, groupId: String)
        fun addMembersToGroup(groupId: String, users: List<User>)
    }

    interface Model {
        suspend fun getAvailableUsers(organizationId: String, groupId: String): List<User>
        suspend fun addMembers(groupId: String, userIds: List<String>)
    }
}