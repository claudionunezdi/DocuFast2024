package com.cnunez.docufast.user.create.MVP
class CreateUserPresenter(private val view: CreateUserContract.View, private val model: CreateUserContract.Model) : CreateUserContract.Presenter {
    override fun createUser(fullName: String, email: String, password: String, workGroups: List<String>, organization: String, role: String) {
        model.createUser(fullName, email, password, workGroups, organization, role) { success, message ->
            if (success) {
                view.showCreateUserSuccess()
            } else {
                view.showCreateUserError(message ?: "Error creating user")
            }
        }
    }
}