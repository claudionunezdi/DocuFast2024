package com.cnunez.docufast.admin.group.detail.presenter

import android.util.Log
import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailPresenter(
    private val view: GroupDetailContract.View,
    private val model: GroupDetailContract.Model
) : GroupDetailContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentGroupId: String? = null


    override fun loadGroupDetails(groupId: String) {
        currentGroupId = groupId
        view.showProgress()
        scope.launch {
            try {
                val group = model.getGroupById(groupId) ?: run {
                    view.onError("Grupo no encontrado")
                    return@launch
                }

                val members = model.getGroupMembers(groupId)
                val files = model.getGroupFiles(groupId)

                view.showGroupName(group.name)
                view.showMembers(members)
                view.showFiles(files)
                view.hideProgress()
            } catch (e: Exception) {
                view.hideProgress()
                view.onError("Error cargando detalles: ${e.message ?: ""}")
                Log.e("GroupDetail", "Error en loadGroupDetails", e)
            }
        }
    }


    override fun checkAdminPermissions(userId: String) {
        scope.launch {
            try {
                val isAdmin = model.isUserAdmin(userId)
                view.setAdminControls(isAdmin)
            } catch (e: Exception) {
                view.setAdminControls(false)
                Log.e("GroupDetail", "Error verificando admin", e)
            }
        }
    }

    override fun removeMemberFromGroup(groupId: String, userId: String) {
        view.showProgress()
        scope.launch {
            try {
                val success = model.removeMemberFromGroup(groupId, userId)
                if (success) {
                    // Obtener y mostrar los miembros actualizados
                    val updatedMembers = model.getGroupMembers(groupId)
                    view.showMembers(updatedMembers)
                    view.onMemberRemoved(userId)
                } else {
                    view.onError("Error al eliminar miembro del grupo")
                }
            } catch (e: Exception) {
                view.onError("Error al eliminar miembro: ${e.message ?: ""}")
            } finally {
                view.hideProgress()
            }
        }
    }

    override fun deleteGroup(groupId: String) {
        view.showProgress()
        scope.launch {
            try {
                model.deleteGroup(groupId)
                view.onGroupDeleted()
            } catch (e: Exception) {
                view.onError("Error eliminando grupo: ${e.message ?: ""}")
            } finally {
                view.hideProgress()
            }
        }
    }

    override fun loadGroupFiles(groupId: String) {
        view.showFileLoadingProgress()
        scope.launch {
            try {
                val files = withContext(Dispatchers.IO) {
                    model.getGroupFiles(groupId)
                }
                view.showFiles(files)
            } catch (e: Exception) {
                view.showFileError("Error cargando archivos: ${e.message ?: "Desconocido"}")
            } finally {
                view.hideFileLoadingProgress()
            }
        }
    }

    fun onDestroy() {
        scope.cancel()
    }
}
