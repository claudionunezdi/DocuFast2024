package com.cnunez.docufast.admin.mainmenu.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.mainmenu.contract.MainMenuContract
import com.cnunez.docufast.admin.mainmenu.presenter.MainMenuPresenter
import com.cnunez.docufast.admin.mainmenu.model.MainMenuModel
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.admin.group.edit.view.ListActivity
import com.cnunez.docufast.admin.user.list.view.UserListActivity
import com.cnunez.docufast.admin.user.create.view.CreateUserActivity
import com.google.firebase.auth.FirebaseUser
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.database.FirebaseDatabase

class MainMenuActivity : BaseActivity(), MainMenuContract.View {

    private lateinit var presenter: MainMenuContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_admin)

        // Inyectar modelo y presentador
        val db = FirebaseDatabase.getInstance()
        val userDao = UserDaoRealtime(db)
        val model = MainMenuModel(userDao)
        presenter = MainMenuPresenter(this, model)

        setupUI()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btnViewGroups).setOnClickListener { presenter.viewGroups() }
        findViewById<Button>(R.id.btnViewUsers).setOnClickListener { presenter.viewUsers() }
        findViewById<Button>(R.id.btnRegisterNewUser).setOnClickListener { presenter.registerNewUser() }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        presenter.loadUserProfile()
    }

    override fun updateUserInfo(name: String, role: String) {
        findViewById<TextView>(R.id.tvWelcome).text = "Bienvenido: $name ($role)"
        val isAdmin = role == "ADMIN"
        findViewById<Button>(R.id.btnRegisterNewUser)
            .visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    override fun showViewGroups() {
        startActivity(Intent(this, ListActivity::class.java))
    }

    override fun showViewUsers() {
        startActivity(Intent(this, UserListActivity::class.java))
    }

    override fun showRegisterNewUser() {
        startActivity(Intent(this, CreateUserActivity::class.java))
    }

    override fun showError(message: String) {
        // Utiliza BaseActivity.showError
        super.showError(message)
    }
}
