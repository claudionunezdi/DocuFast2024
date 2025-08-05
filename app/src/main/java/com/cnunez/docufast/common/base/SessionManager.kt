package com.cnunez.docufast.common.base

import android.content.Context
import android.content.SharedPreferences
import com.cnunez.docufast.MyApp
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

object SessionManager {
    private const val PREFS_NAME = "DocuFastPrefs"
    private const val KEY_USER = "current_user"
    private const val KEY_AUTH_STATE = "auth_state"

    private val prefs: SharedPreferences by lazy {
        MyApp.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val gson = Gson()

    // Cache en memoria para acceso rápido
    private var currentUser: User? = null
    private var isAuthenticated = false

    init {
        // Cargar datos al iniciar
        currentUser = getSavedUser()
        isAuthenticated = prefs.getBoolean(KEY_AUTH_STATE, false) && currentUser != null
    }

    fun saveUserSession(user: User) {
        // Validación crítica para evitar mezcla de usuarios
        if (currentUser?.id != null && currentUser?.id != user.id) {
            clearSession() // Limpia completamente antes de guardar nuevo usuario
        }

        currentUser = user
        isAuthenticated = true
        prefs.edit()
            .putString(KEY_USER, gson.toJson(user))
            .putBoolean(KEY_AUTH_STATE, true)
            .apply()
    }

    fun getCurrentUser(): User? = currentUser

    fun isAuthenticated(): Boolean = isAuthenticated

    fun isAdmin(): Boolean = getCurrentUser()?.role == "ADMIN"

    fun getCurrentOrganization(): String? = currentUser?.organization

    fun logout() {
        currentUser = null
        isAuthenticated = false
        prefs.edit()
            .remove(KEY_USER)
            .putBoolean(KEY_AUTH_STATE, false)
            .apply()
        FirebaseAuth.getInstance().signOut()
    }


  

    // Nuevo método para limpieza completa
    fun clearSession() {
        currentUser = null
        isAuthenticated = false
        prefs.edit()
            .remove(KEY_USER)
            .remove(KEY_AUTH_STATE) // Cambiado de putBoolean(false) a remove
            .apply()
    }

    private fun getSavedUser(): User? {
        val userJson = prefs.getString(KEY_USER, null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
    }

    fun getCurrentUserId(): String? {
        return currentUser?.id
    }




}