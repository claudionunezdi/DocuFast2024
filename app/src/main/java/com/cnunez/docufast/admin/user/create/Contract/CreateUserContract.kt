package com.cnunez.docufast.admin.user.create.Contract

interface CreateUserContract {
    interface View {
        fun showCreateUserSuccess()
        fun showCreateUserError(message: String)
    }

    interface Presenter {
        fun createUser(fullName: String, email: String, password: String, workGroups: List<String>, organization: String, role: String)
    }

    interface Model {
        fun createUser(fullName: String, email: String, password: String, workGroups: List<String>, organization: String, role: String, callback: (Boolean, String?) -> Unit)
    }
}