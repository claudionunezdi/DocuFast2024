
package com.cnunez.docufast.admin.user.create.presenter

import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateUserPresenter(
    private val view: CreateUserContract.View,
    private val model: CreateUserContract.Model
) : CreateUserContract.Presenter {

    private val auth = FirebaseAuth.getInstance()
    private val userDao = UserDaoRealtime(FirebaseDatabase.getInstance())

    override fun createUserWithAdminPassword(
        username: String,
        email: String,
        password: String,
        workGroupIds: List<String>,
        adminPassword: String
    ) {
        val adminId = auth.currentUser?.uid
        if (adminId == null) {
            view.showCreateUserError("Admin no autenticado")
            return
        }
        // Obtener organización del admin
        CoroutineScope(Dispatchers.IO).launch {
            val admin = userDao.getById(adminId)
            val org = admin?.organization ?: ""
            // Construir User con workGroups map
            val wgMap = workGroupIds.associateWith { true }
            val newUser = User(
                id = "",
                name = username,
                email = email,
                organization = org,
                workGroups = wgMap,
                role = "USER",
                stability = 0,
                createdAt = System.currentTimeMillis()
            )
            withContext(Dispatchers.Main) {
                model.createUser(newUser, password, adminPassword) { success, err ->
                    if (success) view.showCreateUserSuccess()
                    else view.showCreateUserError(err ?: "Error desconocido")
                }
            }
        }
    }
}