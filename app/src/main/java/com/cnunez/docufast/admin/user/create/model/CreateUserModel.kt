
package com.cnunez.docufast.admin.user.create.model

import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateUserModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userDao: UserDaoRealtime = UserDaoRealtime(FirebaseDatabase.getInstance())
) : CreateUserContract.Model {

    override fun createUser(
        newUser: User,
        password: String,
        adminPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val admin = auth.currentUser ?: run {
            callback(false, "Admin no autenticado")
            return
        }
        // Reautenticar admin
        val cred = EmailAuthProvider.getCredential(admin.email!!, adminPassword)
        admin.reauthenticate(cred).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                // Crear nuevo usuario en Auth
                auth.createUserWithEmailAndPassword(newUser.email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val created = task.result?.user
                            if (created != null) {
                                newUser.id = created.uid
                                // Guardar en RTDB usando DAO
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        userDao.insert(newUser)
                                        // Cerrar sesión y volver al admin
                                        auth.signOut()
                                        withContext(Dispatchers.Main) { callback(true, null) }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) { callback(false, e.message) }
                                    }
                                }
                            } else {
                                callback(false, "Error al crear usuario en Auth")
                            }
                        } else {
                            callback(false, task.exception?.message)
                        }
                    }
            } else {
                callback(false, "Error autenticando admin: \${authTask.exception?.message}")
            }
        }
    }
}