package com.cnunez.docufast.user.mainmenu.Presenter

import android.util.Log
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.cnunez.docufast.user.mainmenu.Model.MainMenuUserModel
import com.google.firebase.auth.FirebaseAuth

class MainMenuUserPresenter(
    private val view: MainMenuUserContract.View,
    private val model: MainMenuUserModel = MainMenuUserModel()
) : MainMenuUserContract.Presenter {

    override fun loadUserGroups() {
        val currentUser = SessionManager.getCurrentUser()
        if (currentUser == null) {
            view.showError("Usuario no autenticado")
            return
        }

        Log.d("PRESENTER_DEBUG", "Cargando grupos para usuario: ${currentUser.id}")

        model.fetchUserGroups(currentUser.id) { groups, error ->
            if (error != null) {
                Log.e("PRESENTER_ERROR", "Error al cargar grupos: $error")
                view.showError("Error al cargar grupos: $error")
            } else {
                if (groups.isNullOrEmpty()) {
                    Log.d("PRESENTER_DEBUG", "No se encontraron grupos para el usuario")
                } else {
                    Log.d("PRESENTER_DEBUG", "Grupos encontrados: ${groups.size}")
                }
                view.showGroups(groups ?: emptyList())
            }
        }
    }

    override fun onGroupSelected(group: Group) {
        view.showGroupDetail(group)
    }
}