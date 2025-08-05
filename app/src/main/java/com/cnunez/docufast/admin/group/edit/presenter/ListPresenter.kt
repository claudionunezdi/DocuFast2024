package com.cnunez.docufast.admin.group.edit.presenter

import android.util.Log
import com.cnunez.docufast.admin.group.edit.contract.ListContract
import com.cnunez.docufast.common.base.SessionManager

class ListPresenter(
    private val view: ListContract.View,
    private val model: ListContract.Model
) : ListContract.Presenter {

    override fun loadGroups() {
        view.showProgress()
        model.fetchGroups { groups, error ->
            view.hideProgress()
            if (groups != null) {
                Log.d("ListPresenter", "Grupos cargados: ${groups.size}")
                view.showGroups(groups)
            } else {
                Log.e("ListPresenter", "Error al cargar grupos: $error")
                view.showError(error ?: "Error al cargar grupos")
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
