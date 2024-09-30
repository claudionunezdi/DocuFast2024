package com.cnunez.docufast.mainMenuAdmin.View

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.mainMenuAdmin.Contract.MainMenuAdminContract
import com.cnunez.docufast.mainMenuAdmin.Presenter.MainMenuAdminPresenter

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

    override fun showViewUsers() {
        // Implement the logic to show users
    }

    override fun showRegisterNewUser() {
        // Implement the logic to register a new user
    }
}