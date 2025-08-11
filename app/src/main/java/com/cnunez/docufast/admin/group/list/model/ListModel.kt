package com.cnunez.docufast.admin.group.list.model

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
                val user = auth.currentUser ?: return@launch withContext(Dispatchers.Main) {
                    callback(null, "Usuario no autenticado")
                }

                val usr = userDao.getById(user.uid) ?: return@launch withContext(Dispatchers.Main) {
                    callback(null, "Usuario no encontrado")
                }

                if (usr.role != "ADMIN") {
                    return@launch withContext(Dispatchers.Main) {
                        callback(null, "Acceso restringido a administradores")
                    }
                }

                val groups = groupDao.getGroupsByOrganization(usr.organization)
                withContext(Dispatchers.Main) { callback(groups, null) }
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
                    return@launch withContext(Dispatchers.Main) {
                        callback(false, "Permisos insuficientes para eliminar grupo")
                    }
                }

                groupDao.deleteGroup(groupId) // este DAO debe limpiar espejos en /users y /files
                withContext(Dispatchers.Main) { callback(true, null) }
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
