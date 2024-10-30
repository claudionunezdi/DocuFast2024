package com.cnunez.docufast.adminMainMenu.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R

import com.cnunez.docufast.createGroupAdmin.view.AdminCreateGroupActivity
import com.cnunez.docufast.adminMainMenu.Contract.MainMenuAdminContract
import com.cnunez.docufast.adminMainMenu.Presenter.MainMenuAdminPresenter

class MainMenuAdminView : AppCompatActivity(), MainMenuAdminContract.View {
    private lateinit var mainMenuPresenter: MainMenuAdminPresenter
    private lateinit var viewGroupsButton: Button
    private lateinit var viewUsersButton: Button
    private lateinit var registerNewUserButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_admin)

        mainMenuPresenter = MainMenuAdminPresenter(this)

        viewGroupsButton = findViewById(R.id.btnViewGroups)
        viewUsersButton = findViewById(R.id.btnViewUsers)
        registerNewUserButton = findViewById(R.id.btnRegisterNewUser)

        viewGroupsButton.setOnClickListener {
            mainMenuPresenter.viewGroups()
        }

        viewUsersButton.setOnClickListener {
            mainMenuPresenter.viewUsers()
        }

        registerNewUserButton.setOnClickListener {
            mainMenuPresenter.registerNewUser()
        }
    }

    override fun showViewGroups() {
        // Implement the logic to show groups
    }
    override fun showCreateGroups() {
        val intent = Intent(this, AdminCreateGroupActivity::class.java)
        return startActivity(intent)
    }

    override fun showViewUsers() {
        // Implement the logic to show users
    }

    override fun showRegisterNewUser() {
        // Implement the logic to register a new user
    }
}