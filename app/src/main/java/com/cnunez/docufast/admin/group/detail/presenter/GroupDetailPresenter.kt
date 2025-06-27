package com.cnunez.docufast.admin.group.detail.presenter

import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GroupDetailPresenter(
    private val view: GroupDetailContract.View,
    private val model: GroupDetailContract.Model
) : GroupDetailContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadGroupDetails(groupId: String) {
        view.showProgress()
        scope.launch {
            try {
                val members = model.getGroupMembers(groupId)
                val files = model.getGroupFiles(groupId)

                view.hideProgress()
                view.showMembers(members)
                view.showFiles(files)

                // Extra: mostrar nombre (opcional)
                if (members.isNotEmpty()) {
                    view.showGroupName("Miembros del grupo")
                }
            } catch (e: Exception) {
                view.hideProgress()
                view.onError(e.message ?: "Error al cargar detalles del grupo")
            }
        }
    }

    override fun deleteGroup(groupId: String) {
        view.showProgress()
        scope.launch {
            try {
                model.deleteGroup(groupId)
                view.hideProgress()
                view.onGroupDeleted()
            } catch (e: Exception) {
                view.hideProgress()
                view.onError(e.message ?: "Error al eliminar grupo")
            }
        }
    }
}
