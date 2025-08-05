package com.cnunez.docufast.loginMenu.model

import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.loginMenu.contract.LoginMenuContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginMenuModel : LoginMenuContract.Model {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun authenticateUser(email: String, password: String, callback: (User?, String?) -> Unit) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        fetchUserData(userId, callback)
                    } else {
                        callback(null, "Error al obtener ID de usuario")
                    }
                } else {
                    callback(null, "Credenciales incorrectas")
                }
            }
    }

    private fun fetchUserData(userId: String, callback: (User?, String?) -> Unit) {
        database.getReference("users/$userId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)?.apply {
                        id = snapshot.key ?: ""
                    }

                    when {
                        user == null -> {
                            callback(null, "Datos de usuario no encontrados")
                            auth.signOut()
                        }
                        user.role.isBlank() -> { // Validar que tenga rol asignado
                            callback(null, "Usuario no tiene rol asignado")
                            auth.signOut()
                        }
                        !listOf("ADMIN", "USER").contains(user.role.uppercase()) -> { // Validar rol válido
                            callback(null, "Rol de usuario no válido")
                            auth.signOut()
                        }
                        else -> {
                            callback(user, null) // Usuario válido con rol correcto
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, "Error al obtener datos: ${error.message}")
                    auth.signOut()
                }
            })
    }

    override fun checkCurrentSession(callback: (User?) -> Unit) {
        if (SessionManager.getCurrentUser() != null && FirebaseAuth.getInstance().currentUser != null) {
            callback(SessionManager.getCurrentUser())
        } else {
            callback(null)
        }
    }

}