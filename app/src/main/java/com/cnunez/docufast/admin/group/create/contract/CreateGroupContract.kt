package com.cnunez.docufast.admin.group.create.contract

import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User

interface CreateGroupContract {
    interface View {
        fun onGroupCreated(group: Group)
        fun onError(message: String)
        fun getUsersFromOrganization(organization: String)
    }

    interface Presenter {
        fun createGroup(name: String, description: String, members: List<User>)
    }

    interface Model {
        fun saveGroup(
            group: Group,
            onSuccess: () -> Unit,
            onFailure: (exception: Exception) -> Unit
        )

        fun fetchUsersByOrganization(
            organization: String,
            onSuccess: (users: List<User>) -> Unit,
            onFailure: (exception: Exception) -> Unit
        )

        fun fetchAdminUser(
            userId: String,
            onSuccess: (user: User) -> Unit,
            onFailure: (exception: Exception) -> Unit
        )
    }
}