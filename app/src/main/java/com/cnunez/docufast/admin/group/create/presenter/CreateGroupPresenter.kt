package com.cnunez.docufast.admin.group.create.presenter

import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CreateGroupPresenter(
    private val view: CreateGroupContract.View,
    private val model: CreateGroupContract.Model
) : CreateGroupContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun createGroup(name: String, description: String, members: List<User>) {
        view.showProgress()
        scope.launch {
            val result = model.saveGroup(name, description, members)
            view.hideProgress()
            result.onSuccess { group ->
                view.onGroupCreated(group)
            }.onFailure {
                view.showError(it.message ?: "Error desconocido")
            }
        }
    }

    override fun loadUsers(organization: String) {
        view.showProgress()
        scope.launch {
            val result = model.getUsersByOrganization(organization)
            view.hideProgress()
            result.onSuccess { users ->
                view.showUsers(users)
            }.onFailure {
                view.showError(it.message ?: "No se pudieron cargar los usuarios")
            }
        }
    }
}
