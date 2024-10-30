package com.cnunez.docufast.adminLogin.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.adminLogin.contract.LoginAdminContract
import com.cnunez.docufast.adminLogin.model.LoginAdminModel
import com.cnunez.docufast.adminLogin.presenter.LoginAdminPresenter
import com.cnunez.docufast.adminMainMenu.View.MainMenuAdminView

class LoginAdminActivity : AppCompatActivity(), LoginAdminContract.View {
    private lateinit var presenter: LoginAdminContract.Presenter
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_admin)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        presenter = LoginAdminPresenter(this, LoginAdminModel())

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            presenter.login(email, password)
        }
    }

    override fun showLoginSuccess() {
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainMenuAdminView::class.java)
        startActivity(intent)
    }

    override fun showLoginError(message: String) {
        Toast.makeText(this, "Login failed: $message", Toast.LENGTH_SHORT).show()
    }
}