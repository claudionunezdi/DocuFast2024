package com.cnunez.docufast.admin.mainmenu.model

import com.cnunez.docufast.admin.mainmenu.contract.MainMenuContract
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainMenuModel(
    private val userDao: UserDaoRealtime
) : MainMenuContract.Model {

    private val database = FirebaseDatabase.getInstance().reference

    override fun getUserProfile(
        userId: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userDao.getById(userId)
                if (user != null) {
                    withContext(Dispatchers.Main) { onSuccess(user) }
                } else {
                    withContext(Dispatchers.Main) { onError("Usuario no encontrado") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error obteniendo perfil de usuario") }
            }
        }
    }

    override fun setupRoleListener(
        userId: String,
        onRoleChange: (String) -> Unit,
        onError: (String) -> Unit
    ): DatabaseReference {
        val roleRef = database.child("users").child(userId).child("role")
        roleRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                snapshot.getValue(String::class.java)?.let(onRoleChange)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                onError("Error escuchando cambios de rol: ${error.message}")
            }
        })
        return roleRef
    }
}
