package com.cnunez.docufast.common.base

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.loginMenu.view.LoginMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
abstract class BaseActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user == null) {
            navigateToLogin()
        } else {
            checkUserAuthentication(user)
        }
    }

    protected open fun allowedRoles(): Set<String> = setOf("ADMIN")

    protected open fun checkUserAuthentication(user: FirebaseUser) {
        FirebaseDatabase.getInstance()
            .getReference("users").child(user.uid).child("role")
            .get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.getValue(String::class.java)
                if (role != null && allowedRoles().contains(role)) {
                    onUserAuthenticated(user)
                } else {
                    navigateToLogin()
                }
            }
            .addOnFailureListener {
                navigateToLogin()
            }
    }

    protected open fun onUserAuthenticated(user: FirebaseUser) {
        // Para ser sobrescrito
    }

    open fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginMenuActivity::class.java))
        finish()
    }
}
