package com.cnunez.docufast.common.base

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.loginMenu.view.LoginMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseActivity : AppCompatActivity() {
    protected val currentUser by lazy { SessionManager.getCurrentUser() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyAuthentication()
    }

    private fun verifyAuthentication() {
        if (auth.currentUser == null || SessionManager.getCurrentUser() == null) {
            redirectToLogin()
        }
    }

    protected fun redirectToLogin() {
        val intent = Intent(this, LoginMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                SessionManager.logout() // Cambiado de clearSession() a logout()
                redirectToLogin()
            }
        }
    }

    open fun onUserAuthenticated(loggedInUser: FirebaseUser) {
        // Para ser sobrescrito por actividades hijas
    }

    protected fun verifyAdminAccess(block: () -> Unit) {
        if (SessionManager.isAdmin()) {
            block()
        } else {
            Toast.makeText(this, "Solo administradores pueden realizar esta acción", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    protected fun verifyUserAccess(block: () -> Unit) {
        if (SessionManager.getCurrentUser()?.role == "USER") { // Cambiado de isRegularUser()
            block()
        } else {
            Toast.makeText(this, "Acceso no autorizado para tu rol", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    protected fun verifyRoleAndProceed(
        requiredRole: String,
        onAuthorized: () -> Unit,
        onDenied: () -> Unit = { finish() }
    ) {
        when {
            SessionManager.getCurrentUser()?.role == requiredRole -> onAuthorized()
            else -> {
                Toast.makeText(this, "No tienes permisos para esta acción", Toast.LENGTH_SHORT).show()
                onDenied()
            }
        }
    }
}