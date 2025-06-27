package com.cnunez.docufast.admin.group.create.contract

import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User

interface CreateGroupContract {
    interface View {
        fun showProgress()
        fun hideProgress()
        fun showUsers(users: List<User>)
        fun onGroupCreated(group: Group)
        fun onError(message: String)
        fun showError(message: String)
    }

    interface Presenter {
        fun createGroup(name: String, description: String, members: List<User>)
        fun loadUsers(organization: String)
    }

    interface Model {
        suspend fun getUsersByOrganization(organization: String): Result<List<User>>
        suspend fun saveGroup(name: String, description: String, members: List<User>): Result<Group>
    }
}
