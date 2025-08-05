package com.cnunez.docufast.admin.mainmenu.model

import com.cnunez.docufast.admin.mainmenu.contract.MainMenuContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainMenuModel(
    private val userDao: UserDaoRealtime
) : MainMenuContract.Model {

    override fun getUserProfile(
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userDao.getById(userId)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onError("Usuario no encontrado")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error obteniendo perfil")
            }
        }
    }

    override fun setupRoleListener(
        userId: String,
        onRoleChange: (String) -> Unit,
        onError: (String) -> Unit
    ): Pair<DatabaseReference, com.google.firebase.database.ValueEventListener> {
        val roleRef = userDao.usersRef.child(userId).child("role")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                snapshot.getValue(String::class.java)?.let(onRoleChange)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                onError("Error escuchando cambios de rol: ${error.message}")
            }
        }
        roleRef.addValueEventListener(listener)
        return Pair(roleRef, listener)
    }

}
