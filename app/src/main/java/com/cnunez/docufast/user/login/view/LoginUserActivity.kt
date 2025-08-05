package com.cnunez.docufast.user.login.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.mainmenu.view.MainMenuActivity
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.utils.SharedPreferencesManager
import com.cnunez.docufast.user.group.detail.view.GroupDetailActivity
import com.cnunez.docufast.user.login.contract.LoginUserContract
import com.cnunez.docufast.user.login.presenter.LoginUserPresenter
import com.cnunez.docufast.user.mainmenu.view.MainMenuUserActivity

class LoginUserActivity : AppCompatActivity(), LoginUserContract.View {
    private lateinit var presenter: LoginUserContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        presenter = LoginUserPresenter(this)

        val usernameEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showLoginError("Por favor complete todos los campos")
                return@setOnClickListener
            }

            presenter.login(username, password)
        }
    }

    override fun showAdminLoginSuccess(user: User) {
        SharedPreferencesManager.saveUserRole(this, user.role ?: "")
        val intent = Intent(this, MainMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("user_data", user)
        }
        startActivity(intent)
        finish()
    }

    override fun showUserLoginSuccess(user: User) {
        SharedPreferencesManager.saveUserRole(this, user.role ?: "")
        val intent = Intent(this, MainMenuUserActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("user_data", user)
        }
        startActivity(intent)
        finish()
        Toast.makeText(this, "Bienvenido ${user.name}", Toast.LENGTH_SHORT).show()
    }

    override fun showLoginError(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error de inicio de sesión")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}