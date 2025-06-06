package com.cnunez.docufast.admin.user.create.contract

interface CreateUserContract {
    interface View {
        fun showCreateUserSuccess()
        fun showCreateUserError(message: String)
    }

    interface Presenter {
        fun createUserWithAdminPassword(
            username: String,
            email: String,
            password: String,
            workGroups: MutableList<String>,
            adminPassword: String
        )
    }

    interface Model {
        fun createUser(
            username: String,
            email: String,
            password: String,
            workGroups: MutableList<String>,
            organization: String,
            role: String,
            callback: (Boolean, String?) -> Unit
        )
    }
}