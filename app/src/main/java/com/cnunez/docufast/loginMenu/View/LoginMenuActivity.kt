package com.cnunez.docufast.loginMenu.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.adminLogin.view.LoginAdminActivity
import com.cnunez.docufast.loginMenu.Contract.LoginContract
import com.cnunez.docufast.loginMenu.Presenter.LoginMenuPresenter
import com.cnunez.docufast.userLogin.View.LoginUserActivity
import com.cnunez.docufast.registerNewAdmin.View.RegisterNewAdminActivity

class LoginMenuActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_menu)

        presenter = LoginMenuPresenter(this)

        val btnRegisterAdmin = findViewById<Button>(R.id.btn_register_admin)
        val btnLoginAdmin = findViewById<Button>(R.id.btn_login_admin)
        val btnLoginUser = findViewById<Button>(R.id.btn_login_user)

        btnRegisterAdmin.setOnClickListener {
            presenter.onRegisterAdminClicked()
        }

        btnLoginAdmin.setOnClickListener {
            presenter.onLoginAdminClicked()
        }

        btnLoginUser.setOnClickListener {
            presenter.onLoginUserClicked()
        }
    }

    override fun showRegisterAdmin() {
        val intent = Intent(this, RegisterNewAdminActivity::class.java)
        startActivity(intent)
    }

    override fun showLoginAdmin() {
        val intent = Intent(this, LoginAdminActivity::class.java)
        startActivity(intent)
    }

    override fun showLoginUser() {
        val intent = Intent(this, LoginUserActivity::class.java)
        startActivity(intent)
    }
}