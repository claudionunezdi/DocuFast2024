package com.cnunez.docufast.user.mainmenu.Presenter

import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.cnunez.docufast.user.mainmenu.Model.MainMenuUserModel
import com.google.firebase.auth.FirebaseAuth

class MainMenuUserPresenter(
    private val view: MainMenuUserContract.View,
    private val model: MainMenuUserModel = MainMenuUserModel()
) : MainMenuUserContract.Presenter {

    override fun loadUserGroups() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            view.showError("Usuario no autenticado")
            return
        }

        model.fetchUserGroups(currentUser.uid) { groups, error ->
            if (error != null) {
                view.showError(error)
            } else if (groups != null) {
                view.showGroups(groups)
            }
        }
    }

    override fun onGroupSelected(group: Group) {
        view.showGroupDetail(group)
    }
}