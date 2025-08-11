package com.cnunez.docufast.admin.user.detail.presenter

import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import com.cnunez.docufast.admin.user.detail.model.UserDetailModel
import com.cnunez.docufast.common.dataclass.User
import kotlinx.coroutines.*

class UserDetailPresenter(
    private val view: UserDetailContract.View,
    private val model: UserDetailContract.Model = UserDetailModel()
) : UserDetailContract.Presenter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadedUser: User? = null

    override fun loadUser(userId: String) {
        view.showProgress()
        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) { model.fetchUser(userId) }
                if (user == null) {
                    view.onError("Usuario no encontrado")
                    view.hideProgress()
                    return@launch
                }
                loadedUser = user

                val groups = withContext(Dispatchers.IO) { model.fetchAllGroups() }
                view.displayUser(user)
                view.displayGroups(groups, user.workGroups.keys.toSet())
            } catch (e: Exception) {
                view.onError(e.message ?: "Error al cargar usuario")
            } finally {
                view.hideProgress()
            }
        }
    }

    override fun updateUser(
        name: String,
        email: String?,
        selectedGroupIds: List<String>,
        newPassword: String?
    ) {
        val current = loadedUser
        if (current == null) {
            view.onError("Primero carga un usuario")
            return
        }

        // No cambiamos el UID (id)
        val updated = current.copy(
            name = name.ifBlank { current.name },
            email = (email ?: current.email).ifBlank { current.email },
            workGroups = selectedGroupIds.associateWith { true }
        ).copy(id = current.id)

        view.showProgress()
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Guarda el usuario y sincroniza membresías (users<>groups)
                    model.saveUser(updated)
                }

                // Nota: cambiar password de otro usuario no es posible desde el SDK cliente.
                // if (!newPassword.isNullOrBlank()) { /* mostrar aviso si quieres */ }

                loadedUser = updated
                view.onUpdateSuccess()
            } catch (e: Exception) {
                view.onError(e.message ?: "Error al guardar")
            } finally {
                view.hideProgress()
            }
        }
    }

    override fun clear() {
        scope.cancel()
    }
}
