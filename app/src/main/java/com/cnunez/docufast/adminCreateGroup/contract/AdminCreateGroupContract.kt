package com.cnunez.docufast.adminCreateGroup.contract

import com.cnunez.docufast.common.dataclass.Admin
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.WorkGroup

interface AdminCreateGroupContract {
    interface View {
        fun onGroupCreated(group: WorkGroup)
        fun onError(message: String)
        fun getUsers(): List<User>
        fun addUserToAdmin(adminUser: Admin, user: User)
    }

    interface Presenter {
        fun createGroup(name: String, description: String, members: List<User>)
    }

    interface Model {
        fun createGroup(name: String, description: String, members: List<User>): WorkGroup
        fun getGroups(): List<WorkGroup>
    }
}