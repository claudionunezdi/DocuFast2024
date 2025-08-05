// UserListPresenter.kt
package com.cnunez.docufast.admin.user.list.presenter

import com.cnunez.docufast.admin.user.list.contract.UserListContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserListPresenter(
    private val view: UserListContract.View,
    private val model: UserListContract.Model
) : UserListContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadUsers() {
        scope.launch(Dispatchers.IO) {
            try {
                val users = model.fetchUsers()
                withContext(Dispatchers.Main) {
                    view.showUsers(users)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.showError("Error al cargar usuarios: ${e.message ?: "Error desconocido"}")
                }
            }
        }
    }

    override fun deleteUser(userId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                model.deleteUser(userId)
                loadUsers() // Recargar la lista después de eliminar
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.showError("Error al eliminar usuario: ${e.message ?: "Error desconocido"}")
                }
            }
        }
    }

    fun onDestroy() {
        scope.cancel()
    }
}