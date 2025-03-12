package com.cnunez.docufast.admin.user.list.presenter

import com.cnunez.docufast.admin.user.list.contract.UserListContract

class UserListPresenter(
    private val view: UserListContract.View,
    private val model: UserListContract.Model
) : UserListContract.Presenter {
    override fun loadUsers(organization: String) {
        model.fetchUsers(organization) { users, error ->
            if (users != null) {
                view.showUsers(users)
            } else {
                view.showError(error ?: "Unknown error")
            }
        }
    }

    override fun deleteUser(userId: String) {
        model.deleteUser(userId) { success, error ->
            if (success) {
                loadUsers("")
            } else {
                view.showError(error ?: "Error deleting user")
            }
        }
    }
}