package com.cnunez.docufast.admin.group.members.presenter

import com.cnunez.docufast.admin.group.members.contract.AddMembersContract
import com.cnunez.docufast.common.dataclass.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AddMembersPresenter(
    private val view: AddMembersContract.View,
    private val model: AddMembersContract.Model
) : AddMembersContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadAvailableUsers(organizationId: String, groupId: String) {
        scope.launch {
            try {
                val users = model.getAvailableUsers(organizationId, groupId)
                view.showUsers(users)
            } catch (e: Exception) {
                view.showError("Error cargando usuarios: ${e.message}")
            }
        }
    }

    override fun addMembersToGroup(groupId: String, users: List<User>) {
        scope.launch {
            try {
                model.addMembers(groupId, users.map { it.id })
                view.onMembersAdded()
            } catch (e: Exception) {
                view.showError("Error agregando miembros: ${e.message}")
            }
        }
    }

    fun onDestroy() {
        scope.cancel()
    }
}