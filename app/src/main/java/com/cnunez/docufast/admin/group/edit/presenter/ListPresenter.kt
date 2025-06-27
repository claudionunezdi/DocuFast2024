package com.cnunez.docufast.admin.group.edit.presenter

import com.cnunez.docufast.admin.group.edit.contract.ListContract

class ListPresenter(
    private val view: ListContract.View,
    private val model: ListContract.Model
) : ListContract.Presenter {

    override fun loadGroups() {
        view.showProgress()
        model.fetchGroups { groups, error ->
            view.hideProgress()
            if (groups != null) {
                view.showGroups(groups)
            } else {
                view.showError(error ?: "Error desconocido al cargar los grupos")
            }
        }
    }

    override fun deleteGroup(groupId: String) {
        view.showProgress()
        model.deleteGroup(groupId) { success, error ->
            view.hideProgress()
            if (!success) {
                view.showError(error ?: "No se pudo eliminar el grupo")
            } else {
                loadGroups() // Refrescar lista
            }
        }
    }
}
