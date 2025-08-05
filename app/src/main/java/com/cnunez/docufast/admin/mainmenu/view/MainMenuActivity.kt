package com.cnunez.docufast.admin.mainmenu.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.edit.view.ListActivity
import com.cnunez.docufast.admin.mainmenu.contract.MainMenuContract
import com.cnunez.docufast.admin.mainmenu.presenter.MainMenuPresenter
import com.cnunez.docufast.admin.mainmenu.model.MainMenuModel
import com.cnunez.docufast.admin.user.create.view.CreateUserActivity
import com.cnunez.docufast.admin.user.list.view.UserListActivity
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.loginMenu.view.LoginMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainMenuActivity : BaseActivity(), MainMenuContract.View {

    private lateinit var presenter: MainMenuContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_admin)

        // Inicialización con UserDaoRealtime completo
        val userDao = UserDaoRealtime(FirebaseDatabase.getInstance())
        presenter = MainMenuPresenter(this, MainMenuModel(userDao))

        setupUI()
        setupLogoutButton()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btnViewGroups).setOnClickListener { presenter.viewGroups() }
        findViewById<Button>(R.id.btnViewUsers).setOnClickListener { presenter.viewUsers() }
        findViewById<Button>(R.id.btnRegisterNewUser).setOnClickListener { presenter.registerNewUser() }
    }

    private fun setupLogoutButton() {
        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun performLogout() {
        showLoading(true)

        try {
            FirebaseAuth.getInstance().signOut()
            SessionManager.logout() // Cambiado de clearSession() a logout()

            Intent(this, LoginMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(this)
            }
            finishAffinity()

        } catch (e: Exception) {
            showError("Error al cerrar sesión")
        } finally {
            showLoading(false)
        }
    }
    override fun onUserAuthenticated(user: com.google.firebase.auth.FirebaseUser) {
        presenter.loadUserProfile()
    }

    override fun updateUserInfo(name: String, role: String) {
        findViewById<TextView>(R.id.tvWelcome).text = "Bienvenido: $name ($role)"
        findViewById<Button>(R.id.btnRegisterNewUser).visibility =
            if (role == "ADMIN") View.VISIBLE else View.GONE
    }

    override fun showViewGroups() {
        startActivity(Intent(this, ListActivity::class.java))
    }

    override fun showViewUsers() {
        startActivity(Intent(this, UserListActivity::class.java))
    }

    override fun showRegisterNewUser() {
        startActivity(Intent(this, CreateUserActivity::class.java))
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    private fun showLoading(show: Boolean) {
        // Implementa tu lógica de loading aquí
    }
}