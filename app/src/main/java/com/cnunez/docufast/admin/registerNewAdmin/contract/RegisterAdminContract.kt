// RegisterAdminContract.kt
package com.cnunez.docufast.admin.registerNewAdmin.contract

import com.cnunez.docufast.common.dataclass.User

interface RegisterAdminContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showRegisterSuccess()          // cuando un Admin se crea exitosamente
        fun showRegisterError(message: String) // en caso de error al crear Admin

        fun showUserCreateSuccess()        // cuando un USER se crea exitosamente
        fun showUserCreateError(message: String) // en caso de error al crear USER
    }

    interface Presenter {
        fun registerAdmin(
            fullName: String,
            email: String,
            password: String,
            organization: String
        )
        fun createUser(
            fullName: String,
            email: String,
            password: String,
            organization: String
        )
    }

    interface Model {
        fun registerAdmin(
            fullName: String,
            email: String,
            password: String,
            orgId: String,
            callback: (success: Boolean, message: String?) -> Unit
        )
        fun createUser(
            fullName: String,
            email: String,
            password: String,
            orgId: String,
            callback: (success: Boolean, message: String?) -> Unit
        )
    }
}
