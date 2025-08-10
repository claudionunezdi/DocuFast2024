package com.cnunez.docufast.admin.group.list.presenter

import android.util.Log
import com.cnunez.docufast.admin.group.list.contract.ListContract

class ListPresenter(
    private val view: ListContract.View,
    private val model: ListContract.Model
) : ListContract.Presenter {

    override fun loadGroups() {
        view.showProgress()
        model.fetchGroups { groups, error ->
            // Este callback ya se ejecuta en el hilo principal gracias a los cambios en ListModel
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
