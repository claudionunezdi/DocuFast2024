package com.cnunez.docufast.admin.user.list.contract

import com.cnunez.docufast.common.dataclass.User

interface UserListContract {

    interface View {
        fun showUsers(users: List<User>)
        fun showError(message: String)
    }

    interface Presenter {
        fun loadUsers(organization: String)
        fun deleteUser(userId: String)
    }

    interface Model {
        fun fetchUsers(organization: String, callback: (List<User>?, String?) -> Unit)
        fun deleteUser(userId: String, callback: (Boolean, String?) -> Unit)
    }
}