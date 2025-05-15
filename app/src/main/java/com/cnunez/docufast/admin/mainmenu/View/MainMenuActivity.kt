package com.cnunez.docufast.admin.mainmenu.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.edit.view.ListActivity
import com.cnunez.docufast.admin.mainmenu.Contract.MainMenuContract
import com.cnunez.docufast.admin.mainmenu.Presenter.MainMenuPresenter
import com.cnunez.docufast.admin.user.create.view.CreateUserActivity
import com.cnunez.docufast.admin.user.list.view.UserListActivity
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.utils.Utils
import com.google.firebase.auth.FirebaseUser


class MainMenuActivity :BaseActivity(), MainMenuContract.View {
    private lateinit var mainMenuPresenter: MainMenuPresenter
    private lateinit var viewGroupsButton: Button
    private lateinit var viewUsersButton: Button
    private lateinit var registerNewUserButton: Button
    private lateinit var tvWelcome: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_admin)

        mainMenuPresenter = MainMenuPresenter(this)

        viewGroupsButton = findViewById(R.id.btnViewGroups)
        viewUsersButton = findViewById(R.id.btnViewUsers)
        registerNewUserButton = findViewById(R.id.btnRegisterNewUser)
        tvWelcome = findViewById(R.id.tvWelcome)

        val userRole = Utils.getUserRole(this)
        if (userRole == "admin") {
            viewGroupsButton.setOnClickListener {
                mainMenuPresenter.viewGroups()
            }

            viewUsersButton.setOnClickListener {
                mainMenuPresenter.viewUsers()
            }

            registerNewUserButton.setOnClickListener {
                mainMenuPresenter.registerNewUser()
            }

            Toast.makeText(this, "Admin permissions", Toast.LENGTH_SHORT).show()

            tvWelcome.text = "Welcome ${mainMenuPresenter.getUserName()}"
        } else {
            Toast.makeText(this, "No admin permissions", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        if (user.isAnonymous) {
            Toast.makeText(this, "Anonymous user detected. Redirecting to login.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Welcome ${user.email}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showViewGroups() {
        val intent = Intent(this, ListActivity::class.java)
        startActivity(intent)
    }



    override fun showViewUsers() {
        val intent = Intent(this, UserListActivity::class.java)
        startActivity(intent)
    }

    override fun showRegisterNewUser() {
        val intent = Intent(this, CreateUserActivity::class.java)
        startActivity(intent)
    }
}