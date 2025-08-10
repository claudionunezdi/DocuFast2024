package com.cnunez.docufast.admin.group.list.model

import android.util.Log
import com.cnunez.docufast.admin.group.list.contract.ListContract
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class ListModel(
    private val userDao: UserDaoRealtime,
    private val groupDao: GroupDaoRealtime
) : ListContract.Model {

    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun fetchGroups(callback: (List<Group>?, String?) -> Unit) {
        scope.launch {
            try {
                val user = auth.currentUser ?: run {
                    withContext(Dispatchers.Main) {
                        callback(null, "Usuario no autenticado")
                    }
                    return@launch
                }

                val usr = userDao.getById(user.uid) ?: run {
                    withContext(Dispatchers.Main) {
                        callback(null, "Usuario no encontrado")
                    }
                    return@launch
                }

                if (!usr.isAdmin()) {
                    withContext(Dispatchers.Main) {
                        callback(null, "Acceso restringido a administradores")
                    }
                    return@launch
                }

                val groups = groupDao.getGroupsByOrganization(usr.organization)
                Log.d("GroupDao", "Buscando grupos para org: ${usr.organization}")
                Log.d("ListModel", "Grupos encontrados para org ${usr.organization}: ${groups.size}")

                withContext(Dispatchers.Main) {
                    callback(groups, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null, "Error: ${e.message}")
                }
            }
        }
    }

    override fun deleteGroup(groupId: String, callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "Usuario no autenticado")
            return
        }

        scope.launch {
            try {
                val usr = userDao.getById(user.uid)
                if (usr?.role != "ADMIN") {
                    withContext(Dispatchers.Main) {
                        callback(false, "Permisos insuficientes para eliminar grupo")
                    }
                    return@launch
                }

                groupDao.deleteGroup(groupId)
                withContext(Dispatchers.Main) {
                    callback(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, e.message ?: "Error al eliminar el grupo")
                }
            }
        }
    }

    fun clear() {
        scope.cancel()
    }
}
