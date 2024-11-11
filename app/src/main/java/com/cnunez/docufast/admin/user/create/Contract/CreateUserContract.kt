package com.cnunez.docufast.user.create.MVP

interface CreateUserContract {
    interface View {
        fun showCreateUserSuccess()
        fun showCreateUserError(message: String)
    }

    interface Presenter {
        fun createUser(fullName: String, email: String, password: String)
    }

    interface Model {
        fun createUser(fullName: String, email: String, password: String, callback: (Boolean, String?) -> Unit)
    }
}