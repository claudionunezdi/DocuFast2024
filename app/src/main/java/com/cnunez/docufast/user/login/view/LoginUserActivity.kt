package com.cnunez.docufast.user.login.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.mainmenu.View.MainMenuActivity
import com.cnunez.docufast.common.Utils
import com.cnunez.docufast.user.login.presenter.LoginUserPresenter
import com.cnunez.docufast.user.login.contract.LoginUserContract

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.user.group.detail.view.GroupDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginUserActivity : AppCompatActivity(), LoginUserContract.View {
    private lateinit var presenter: LoginUserContract.Presenter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        presenter = LoginUserPresenter(this)

        val usernameEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            presenter.login(username, password)
        }
    }

    override fun showAdminLoginSuccess(user: User) {
        Utils.saveUserRole(this, user.role)
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
    }

    override fun showUserLoginSuccess(user: User) {
        Utils.saveUserRole(this, user.role)
        val intent = Intent(this, GroupDetailActivity::class.java)
        startActivity(intent)
        Toast.makeText(this, "User: Bienvenido a ${user.organization} sr ${user.name}", Toast.LENGTH_SHORT).show()
    }

    override fun showLoginError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}