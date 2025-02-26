package com.cnunez.docufast.admin.mainmenu.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.edit.view.ListActivity
import com.cnunez.docufast.admin.mainmenu.Contract.MainMenuContract
import com.cnunez.docufast.admin.mainmenu.Presenter.MainMenuPresenter
import com.cnunez.docufast.admin.user.create.View.CreateUserActivity

class MainMenuActivity : AppCompatActivity(), MainMenuContract.View {
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

        viewGroupsButton.setOnClickListener {
            mainMenuPresenter.viewGroups()
        }

        viewUsersButton.setOnClickListener {
            mainMenuPresenter.viewUsers()
        }

        registerNewUserButton.setOnClickListener {
            mainMenuPresenter.registerNewUser()
        }

        tvWelcome.text = "Welcome, ${mainMenuPresenter.getUserName()}"

    }

    override fun showViewGroups() {
        val intent = Intent(this, ListActivity::class.java)
        startActivity(intent)
    }

    override fun showCreateGroups() {
        // TODO: crear la actividad CreateActivitySHow
    }

    override fun showViewUsers() {
    }

    override fun showRegisterNewUser() {
        val intent = Intent(this, CreateUserActivity::class.java)
        startActivity(intent)
    }
}