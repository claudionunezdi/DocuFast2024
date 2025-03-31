package com.cnunez.docufast.loginMenu.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.registerNewAdmin.View.registerActivity

import com.cnunez.docufast.loginMenu.Contract.LoginMenuContract
import com.cnunez.docufast.loginMenu.Presenter.LoginMenuPresenter
import com.cnunez.docufast.user.login.view.LoginUserActivity


class LoginMenuActivity : AppCompatActivity(), LoginMenuContract.View {
    private lateinit var loginMenuPresenter: LoginMenuPresenter
    private lateinit var registerAdminButton: Button

    private lateinit var loginUserButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_menu)

        loginMenuPresenter = LoginMenuPresenter(this)
        registerAdminButton = findViewById(R.id.btn_register_admin)

        loginUserButton= findViewById(R.id.btn_login_user)

        registerAdminButton.setOnClickListener{
            loginMenuPresenter.onRegisterAdminClicked()
        }



        loginUserButton.setOnClickListener {
            loginMenuPresenter.onLoginUserClicked()
        }

        
        




        }

    override fun showRegisterAdmin() {
        val intent = Intent(this, registerActivity::class.java )
        startActivity(intent)
    }



    override fun showLoginUser() {
        val intent = Intent(this, LoginUserActivity::class.java)
        startActivity(intent)



    }


}

