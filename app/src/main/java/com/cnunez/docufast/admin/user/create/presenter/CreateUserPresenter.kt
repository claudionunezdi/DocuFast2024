package com.cnunez.docufast.admin.user.create.presenter

import android.util.Log
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.manager.GroupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateUserPresenter(
    private val view: CreateUserContract.View,
    private val model: CreateUserContract.Model,
    private val groupManager: GroupManager
) : CreateUserContract.Presenter {

    private var isViewActive = true

    override fun attachView() { isViewActive = true }
    override fun detachView() { isViewActive = false }

    override fun createUserWithAdminPassword(
        username: String,
        email: String,
        password: String,
        workGroupIds: List<String>,
        adminPassword: String
    ) {
        if (!isViewActive) {
            Log.w("CreateUserPresenter", "Intento de creación con vista inactiva")
            return
        }

        if (listOf(username, email, password, adminPassword).any { it.isBlank() }) {
            view.showCreateUserError("Todos los campos son obligatorios")
            return
        }
        if (workGroupIds.isEmpty()) {
            view.showCreateUserError("Se debe seleccionar al menos un grupo")
            return
        }

        SessionManager.getCurrentOrganization()?.let { organization ->
            createNewUser(
                username.trim(),
                email.trim(),
                password,
                workGroupIds,
                organization,
                adminPassword
            )
        } ?: view.showCreateUserError("No se pudo obtener la organización actual")
    }

    private fun createNewUser(
        username: String,
        email: String,
        password: String,
        workGroupIds: List<String>,
        organization: String,
        adminPassword: String
    ) {
        val newUser = User(
            id = "", // Se asignará con el UID real del Auth
            name = username,
            email = email,
            role = "USER",
            workGroups = workGroupIds.associateWith { true },
            organization = organization,
            createdAt = System.currentTimeMillis()
        )

        model.createUser(newUser, password, adminPassword) { success, error, newUserId ->
            if (!isViewActive) return@createUser

            if (success && !newUserId.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    handleGroupAssignments(workGroupIds, newUserId)
                }
            } else {
                view.showCreateUserError(error ?: "Error desconocido al crear usuario")
            }
        }
    }

    private suspend fun handleGroupAssignments(groupIds: List<String>, userId: String) {
        try {
            val failedAssignments = mutableListOf<String>()

            groupIds.forEach { groupId ->
                try {
                    groupManager.addMemberToGroup(groupId, userId)
                } catch (e: Exception) {
                    Log.e("CreateUserPresenter", "Error asignando a grupo $groupId", e)
                    failedAssignments.add(groupId)
                }
            }

            withContext(Dispatchers.Main) {
                if (failedAssignments.isEmpty()) {
                    view.showCreateUserSuccess()
                } else {
                    view.showCreateUserError(
                        "Usuario creado pero con errores en ${failedAssignments.size} grupos"
                    )
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                view.showCreateUserError("Error crítico al asignar grupos: ${e.message}")
            }
        }
    }
}
