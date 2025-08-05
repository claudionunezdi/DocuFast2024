package com.cnunez.docufast.admin.mainmenu.contract

import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

interface MainMenuContract {
    interface View {
        fun showViewGroups()
        fun showViewUsers()
        fun showRegisterNewUser()
        fun updateUserInfo(name: String, role: String)
        fun showError(message: String)
    }

    interface Presenter {
        fun viewGroups()
        fun viewUsers()
        fun registerNewUser()

        fun loadUserProfile()
        fun verifyAdminPermissions()
        fun destroy()
    }

    interface Model {
        fun getUserProfile(
            userId: String,
            onSuccess: (User) -> Unit,
            onError: (String) -> Unit
        )

        fun setupRoleListener(
            userId: String,
            onRoleChange: (String) -> Unit,
            onError: (String) -> Unit
        ): Pair<DatabaseReference, ValueEventListener> // Cambiado a devolver Pair
    }
}