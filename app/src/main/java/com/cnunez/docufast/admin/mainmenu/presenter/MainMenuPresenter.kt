package com.cnunez.docufast.admin.mainmenu.presenter

import com.cnunez.docufast.admin.mainmenu.contract.MainMenuContract
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DatabaseReference

class MainMenuPresenter(
    private val view: MainMenuContract.View,
    private val model: MainMenuContract.Model
) : MainMenuContract.Presenter {

    private val auth = FirebaseAuth.getInstance()
    private var roleRef: com.google.firebase.database.DatabaseReference? = null
    private var roleListener: com.google.firebase.database.ValueEventListener? = null

    override fun viewGroups() = view.showViewGroups()
    override fun viewUsers() = view.showViewUsers()
    override fun registerNewUser() = view.showRegisterNewUser()

    override fun loadUserProfile() {
        auth.currentUser?.uid?.let { userId ->
            model.getUserProfile(
                userId,
                onSuccess = { user ->
                    view.updateUserInfo(user.name, user.role)
                    if (user.role == "ADMIN") verifyAdminPermissions()
                },
                onError = { error -> view.showError(error) }
            )
        } ?: view.showError("Usuario no autenticado")
    }

    private var roleRefAndListener: Pair<DatabaseReference, com.google.firebase.database.ValueEventListener>? = null

    override fun verifyAdminPermissions() {
        auth.currentUser?.uid?.let { userId ->
            roleRefAndListener = model.setupRoleListener(
                userId,
                onRoleChange = { role ->
                    if (role != "ADMIN") {
                        view.showError("Permisos de administrador revocados")
                        view.updateUserInfo("Usuario", role)
                    }
                },
                onError = { error -> view.showError(error) }
            )
        }
    }
    override fun destroy() {
        roleRefAndListener?.let { (ref, listener) ->
            ref.removeEventListener(listener)
        }
    }
}