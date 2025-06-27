// UserListPresenter.kt
package com.cnunez.docufast.admin.user.list.presenter

import com.cnunez.docufast.admin.user.list.contract.UserListContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UserListPresenter(
    private val view: UserListContract.View,
    private val model: UserListContract.Model
) : UserListContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadUsers() {
        scope.launch {
            try {
                val users = model.fetchUsers()
                view.showUsers(users)
            } catch (e: Exception) {
                view.showError(e.message.orEmpty())
            }
        }
    }

    override fun deleteUser(userId: String) {
        scope.launch {
            try {
                model.deleteUser(userId)
                // Refresca la lista tras borrar
                loadUsers()
            } catch (e: Exception) {
                view.showError(e.message.orEmpty())
            }
        }
    }
}
