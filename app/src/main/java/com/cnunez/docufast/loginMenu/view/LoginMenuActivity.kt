package com.cnunez.docufast.loginMenu.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.mainmenu.view.MainMenuActivity

import com.cnunez.docufast.admin.registerNewAdmin.view.RegisterAdminActivity
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.loginMenu.contract.LoginMenuContract
import com.cnunez.docufast.loginMenu.model.LoginMenuModel
import com.cnunez.docufast.loginMenu.presenter.LoginMenuPresenter
import com.cnunez.docufast.user.login.view.LoginUserActivity
import com.cnunez.docufast.common.dataclass.User

class LoginMenuActivity : AppCompatActivity(), LoginMenuContract.View {
    private lateinit var presenter: LoginMenuPresenter
    private lateinit var registerAdminButton: Button
    private lateinit var loginUserButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_menu)

        // Inicialización del presenter con modelo
        presenter = LoginMenuPresenter(this, LoginMenuModel())

        registerAdminButton = findViewById(R.id.btn_register_admin)
        loginUserButton = findViewById(R.id.btn_login_user)

        registerAdminButton.setOnClickListener {
            presenter.onRegisterAdminClicked()
        }

        loginUserButton.setOnClickListener {
            presenter.onLoginUserClicked()
        }
    }
    override fun showRegisterAdmin() {
        startActivity(Intent(this, RegisterAdminActivity::class.java))
    }

    override fun showLoginUser() {
        startActivity(Intent(this, LoginUserActivity::class.java))
    }

    override fun showMainMenu(user: User) {
        SessionManager.saveUserSession(user)
        val intent = Intent(this, MainMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun showLoading(show: Boolean) {
        Toast.makeText(this, "Cargando...", Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


