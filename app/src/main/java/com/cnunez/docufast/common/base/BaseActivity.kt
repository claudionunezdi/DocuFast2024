package com.cnunez.docufast.common.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.common.utils.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkUserAuthentication()
    }

    // Cambiar a open para permitir la sobrescritura
    open fun checkUserAuthentication() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.let { onUserAuthenticated(it) }
                } else {
                    showError("Authentication failed. Please restart the app.")
                    finish()
                }
            }
        } else {
            val userRole = SharedPreferencesManager.getUserRole(this)
            if (userRole.isNullOrEmpty()) {
                showError("User role is missing. Please log in again.")
                finish()
            } else {
                onUserAuthenticated(currentUser)
            }
        }
    }

    abstract fun onUserAuthenticated(user: FirebaseUser)

    open fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}