package com.cnunez.docufast.admin.user.list.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.create.view.CreateUserActivity
import com.cnunez.docufast.admin.user.detail.view.UserDetailActivity
import com.cnunez.docufast.admin.user.list.contract.UserListContract
import com.cnunez.docufast.admin.user.list.model.UserListModel
import com.cnunez.docufast.admin.user.list.presenter.UserListPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.adapters.UserAdapterUnified
import com.cnunez.docufast.common.dataclass.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import android.view.View
import androidx.core.view.children

class UserListActivity : BaseActivity(), UserListContract.View {
    private lateinit var presenter: UserListPresenter
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: UserAdapterUnified

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        setupRecyclerView()
        setupFab()
        initializePresenter()
    }

    private fun setupRecyclerView() {
        recycler = findViewById(R.id.recyclerViewUsers)
        adapter = UserAdapterUnified.forEditing(
            users = emptyList(),
            onEdit = { user -> navigateToUserDetail(user.id) },
            onDelete = { user ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar usuario")
                    .setMessage("¿Eliminar a ${user.name}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        presenter.deleteUser(user.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        ).apply {
            // Añade esto para debug
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    Log.d("AdapterDebug", "Datos cambiados. Total: ${itemCount}")
                    recycler.children.forEachIndexed { i, view ->
                        Log.d("AdapterDebug", "Vista $i - ${view.findViewById<TextView>(R.id.textViewUserName)?.text}")
                    }
                }
            })
        }

        recycler.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = this@UserListActivity.adapter
            setHasFixedSize(true)
            itemAnimator = null // Desactiva animaciones temporalmente para debug
        }
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddUser).setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
    }

    private fun initializePresenter() {
        presenter = UserListPresenter(this, UserListModel())
    }

    override fun onResume() {
        super.onResume()
        presenter.loadUsers() // Cargar datos cada vez que la actividad se vuelve visible
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        presenter.loadUsers()
    }

    override fun showUsers(users: List<User>) {

        adapter.updateUsers(users)

        // Hotfix para forzar redraw
        recycler.post {
            adapter.notifyDataSetChanged()
            Log.d("Hotfix", "Forzando redraw con ${users.size} usuarios")
        }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("UserListActivity", message)
    }

    private fun navigateToUserDetail(userId: String) {
        Intent(this, UserDetailActivity::class.java).apply {
            putExtra("USER_ID", userId)
            startActivity(this)
        }
    }
}