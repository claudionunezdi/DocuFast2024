package com.cnunez.docufast.admin.user.create.contract

import com.cnunez.docufast.common.dataclass.User

interface CreateUserContract {
    interface View {
        fun showCreateUserSuccess()
        fun showCreateUserError(message: String)
    }

    interface Presenter {
        fun attachView()
        fun detachView()
        fun createUserWithAdminPassword(
            username: String,
            email: String,
            password: String,
            workGroupIds: List<String>,
            adminPassword: String
        )
    }

    interface Model {
        fun createUser(
            newUser: User,
            password: String,
            adminPassword: String,
            callback: (success: Boolean, error: String?, newUserId: String?) -> Unit
        )
    }
}
