package com.cnunez.docufast.admin.group.edit.presenter

import com.cnunez.docufast.admin.group.edit.contract.ListContract
import com.cnunez.docufast.common.dataclass.Group

class ListPresenter(
    private val view: ListContract.View,
    private val model: ListContract.Model
) : ListContract.Presenter {

    override fun loadGroups() {
        model.fetchGroups { groups, error ->
            if (error == null && groups != null) {
                view.showGroups(groups)
            } else {
                view.showError(error ?: "Error al cargar los grupos.")
            }
        }
    }

    override fun deleteGroup(groupId: String) {
        model.deleteGroup(groupId) { success, error ->
            if (success) {
                loadGroups() // Refrescar la lista tras la eliminación
            } else {
                view.showError(error ?: "Error desconocido al eliminar el grupo.")
            }
        }
    }
}