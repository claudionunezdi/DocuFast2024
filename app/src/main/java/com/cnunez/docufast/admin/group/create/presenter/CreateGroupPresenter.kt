package com.cnunez.docufast.admin.group.create.presenter

import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
class CreateGroupPresenter(
    private val view: CreateGroupContract.View,
    private val model: CreateGroupContract.Model
) : CreateGroupContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun createGroup(name: String, description: String, members: List<User>) {
        when {
            name.isBlank() -> view.showError("El nombre del grupo es requerido")
            description.isBlank() -> view.showError("La descripción es requerida")
            members.isEmpty() -> view.showError("Selecciona al menos un miembro")
            else -> {
                view.showProgress()
                scope.launch {
                    model.saveGroup(name, description, members)
                        .fold(
                            onSuccess = { group ->
                                view.hideProgress()
                                view.onGroupCreated(group)
                            },
                            onFailure = { error ->
                                view.hideProgress()
                                view.showError(error.message ?: "Error desconocido al crear grupo")
                            }
                        )
                }
            }
        }
    }

    override fun loadUsers(organization: String) {
        view.showProgress()
        scope.launch {
            model.getUsersByOrganization(organization)
                .fold(
                    onSuccess = { users ->
                        view.hideProgress()
                        view.showUsers(users)
                    },
                    onFailure = { error ->
                        view.hideProgress()
                        view.showError(error.message ?: "Error al cargar usuarios")
                    }
                )
        }
    }



    fun onDestroy() {
        scope.coroutineContext.cancel()
    }
}