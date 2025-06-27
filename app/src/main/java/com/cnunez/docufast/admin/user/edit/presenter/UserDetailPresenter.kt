package com.cnunez.docufast.admin.user.detail.presenter

import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDetailPresenter(
    private val view: UserDetailContract.View,
    private val model: UserDetailContract.Model
) : UserDetailContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentUserId: String? = null

    override fun loadUser(userId: String) {
        currentUserId = userId
        view.showProgress()
        scope.launch {
            try {
                val user = model.fetchUser(userId)
                    ?: throw Exception("Usuario no encontrado")
                val groups = model.fetchAllGroups()
                view.displayUser(user)
                view.displayGroups(groups, user.workGroups.keys)
            } catch (e: Exception) {
                view.onError(e.message.orEmpty())
            } finally {
                view.hideProgress()
            }
        }
    }

    override fun updateUser(name: String, email: String, selectedGroupIds: List<String>) {
        val userId = currentUserId
        if (userId == null) {
            view.onError("ID de usuario inválido")
            return
        }
        view.showProgress()
        scope.launch {
            try {
                val existing = model.fetchUser(userId)
                    ?: throw Exception("Usuario no encontrado")
                val updated = existing.copy(
                    name = name,
                    email = email,
                    workGroups = selectedGroupIds.associateWith { true }.ifEmpty { emptyMap()}
                )
                model.saveUser(updated)
                withContext(Dispatchers.Main) { view.onUpdateSuccess() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { view.onError(e.message.orEmpty()) }
            } finally {
                view.hideProgress()
            }
        }
    }

    override fun clear() {
        scope.coroutineContext.cancelChildren()
    }
}