package com.cnunez.docufast.admin.user.list.contract

import com.cnunez.docufast.common.dataclass.User

interface UserListContract {
    interface View {
        fun showUsers(users: List<User>)
        fun showError(message: String)  // ← ¡ESTO ES LO QUE FALTA!
    }

    interface Presenter {
        fun loadUsers()
        fun deleteUser(userId: String)
    }

    interface Model {
        suspend fun fetchUsers(): List<User>
        suspend fun deleteUser(userId: String)
    }
}
