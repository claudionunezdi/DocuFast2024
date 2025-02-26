package com.cnunez.docufast.user.login.View

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.mainmenu.View.MainMenuActivity
import com.cnunez.docufast.user.login.Presenter.LoginUserPresenter
import com.cnunez.docufast.user.login.Contract.LoginUserContract
import com.cnunez.docufast.user.mainmenu.View.MainMenuUserActivity
import com.cnunez.docufast.common.dataclass.User

class LoginUserActivity : AppCompatActivity(), LoginUserContract.View {
    private lateinit var presenter: LoginUserContract.Presenter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        presenter = LoginUserPresenter(this)

        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            presenter.login(username, password)
        }
    }

    override fun showAdminLoginSuccess(user: User) {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        Toast.makeText(this, "Admin: Bienvenido Administrador ${user.name}", Toast.LENGTH_SHORT).show()
    }

    override fun showUserLoginSuccess(user: User) {
        val intent = Intent(this, MainMenuUserActivity::class.java)
        startActivity(intent)
        Toast.makeText(this, "User: Bienvenido a ${user.organization} sr ${user.name}", Toast.LENGTH_SHORT).show()
    }

    override fun showLoginError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}