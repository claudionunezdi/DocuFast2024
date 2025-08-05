package com.cnunez.docufast.admin.user.detail.presenter

import android.content.ContentValues.TAG
import android.util.Log
import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserDetailPresenter(
    private val view: UserDetailContract.View,
    private val model: UserDetailContract.Model,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : UserDetailContract.Presenter {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentUserId: String? = null

    override fun loadUser(userId: String) {
        Log.d("UserDetailPresenter", "Loading user with ID: $userId")
        currentUserId = userId
        view.showProgress()
        scope.launch {
            try {
                Log.d("UserDetailPresenter", "Fetching user data...")
                val user = model.fetchUser(userId) ?: throw Exception("Usuario no encontrado")
                Log.d("UserDetailPresenter", "User data fetched, fetching groups...")
                val groups = model.fetchAllGroups()

                withContext(Dispatchers.Main) {
                    Log.d("UserDetailPresenter", "Displaying user and groups")
                    view.displayUser(user)
                    view.displayGroups(groups, user.workGroups.keys)
                }
            } catch (e: Exception) {
                Log.e("UserDetailPresenter", "Error loading user", e)
                withContext(Dispatchers.Main) {
                    view.onError("Error al cargar usuario: ${e.message ?: "Desconocido"}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    view.hideProgress()
                }
            }
        }
    }

    override fun updateUser(
        name: String,
        email: String?,  // Ahora es nullable (opcional)
        selectedGroupIds: List<String>,
        newPassword: String?  // Ya era nullable
    ) {
        val userId = currentUserId ?: run {
            view.onError("ID de usuario inválido")
            return
        }

        view.showProgress()
        scope.launch {
            try {
                // 1. Obtener usuario existente
                val existingUser = model.fetchUser(userId) ?: throw Exception("Usuario no encontrado")

                // 2. Actualizar datos solo si hay cambios
                val updatedUser = existingUser.copy(
                    name = name,
                    email = email ?: existingUser.email, // Mantener email actual si no se proporciona
                    workGroups = selectedGroupIds.associateWith { true }
                )

                // 3. Actualizar Auth (email y contraseña) solo si hay cambios
                val authUser = firebaseAuth.currentUser
                if (authUser?.uid == userId) {
                    // Cambiar email (si es nuevo y diferente)
                    email?.takeIf { it.isNotBlank() && it != existingUser.email }?.let { newEmail ->
                        authUser.updateEmail(newEmail).await()
                        Log.d(TAG, "Email actualizado en Auth")
                    }

                    // Cambiar contraseña (si se proporciona)
                    newPassword?.takeIf { it.isNotBlank() }?.let { password ->
                        if (password.length < 6) throw Exception("La contraseña debe tener al menos 6 caracteres")
                        authUser.updatePassword(password).await()
                        Log.d(TAG, "Contraseña actualizada en Auth")
                    }
                }

                // 4. Guardar en RTDB solo si hay cambios
                if (updatedUser != existingUser) {
                    model.saveUser(updatedUser)
                    Log.d(TAG, "Datos actualizados en RTDB")
                }

                view.onUpdateSuccess()
            } catch (e: Exception) {
                view.onError("Error al actualizar: ${e.message ?: "Desconocido"}")
            } finally {
                view.hideProgress()
            }
        }
    }

    private suspend fun updateUserPassword(userId: String, newPassword: String) {
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                if (user?.uid != userId) {
                    throw SecurityException("No tienes permisos para cambiar esta contraseña")
                }
                user.updatePassword(newPassword).await()
                Log.d("UserDetailPresenter", "Contraseña actualizada con éxito")
            } catch (e: Exception) {
                Log.e("UserDetailPresenter", "Error al actualizar contraseña", e)
                throw Exception("Error al cambiar contraseña: ${e.message}")
            }
        }
    }

    private suspend fun updateAuthEmail(userId: String, newEmail: String) {
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                if (user?.uid != userId) {
                    throw SecurityException("No tienes permisos para cambiar este email")
                }
                user.updateEmail(newEmail).await()
                Log.d("UserDetailPresenter", "Email actualizado en Auth con éxito")
            } catch (e: Exception) {
                Log.e("UserDetailPresenter", "Error al actualizar email", e)
                throw Exception("Error al cambiar email: ${e.message}")
            }
        }
    }

    private suspend fun updateAuthDisplayName(userId: String, name: String) {
        withContext(Dispatchers.IO) {
            try {
                val user = firebaseAuth.currentUser
                if (user?.uid != userId) {
                    return@withContext
                }
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                Log.d("UserDetailPresenter", "Nombre actualizado en Auth con éxito")
            } catch (e: Exception) {
                Log.e("UserDetailPresenter", "Error al actualizar nombre", e)
                // No lanzamos excepción porque esto no es crítico
            }
        }
    }

    override fun clear() {
        scope.coroutineContext.cancelChildren()
    }
}