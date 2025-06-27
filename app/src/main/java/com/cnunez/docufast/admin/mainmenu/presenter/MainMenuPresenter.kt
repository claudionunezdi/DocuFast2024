package com.cnunez.docufast.admin.mainmenu.presenter

import com.cnunez.docufast.admin.mainmenu.contract.MainMenuContract
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth

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
        val uid = auth.currentUser?.uid
        if (uid == null) {
            view.showError("Usuario no autenticado")
            return
        }
        model.getUserProfile(uid,
            onSuccess = { user: User ->
                view.updateUserInfo(user.name, user.role)
                if (user.role == "ADMIN") verifyAdminPermissions()
            },
            onError = { err -> view.showError(err) }
        )
    }

    override fun verifyAdminPermissions() {
        val uid = auth.currentUser?.uid ?: return
        roleRef = model.setupRoleListener(uid,
            onRoleChange = { role ->
                if (role != "ADMIN") {
                    view.showError("Permisos de administrador revocados")
                    view.updateUserInfo("Usuario", role)
                }
            },
            onError = { err -> view.showError(err) }
        )
    }

    override fun destroy() {
        roleListener?.let { roleRef?.removeEventListener(it) }
    }
}
