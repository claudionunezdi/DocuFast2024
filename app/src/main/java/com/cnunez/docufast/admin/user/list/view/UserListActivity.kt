package com.cnunez.docufast.admin.user.list.view

import android.content.Intent
import android.os.Bundle
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.create.view.CreateUserActivity
import com.cnunez.docufast.admin.user.edit.view.UserDetailActivity
import com.cnunez.docufast.admin.user.list.contract.UserListContract
import com.cnunez.docufast.admin.user.list.model.UserListModel
import com.cnunez.docufast.admin.user.list.presenter.UserListPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.adapters.UserListAdapter
import com.cnunez.docufast.common.dataclass.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser

class UserListActivity : BaseActivity(), UserListContract.View {
    private lateinit var presenter: UserListContract.Presenter
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        recycler = findViewById(R.id.recyclerViewUsers)
        adapter = UserListAdapter(mutableListOf(),
            onEditClickListener = { user ->
                startActivity(Intent(this, UserDetailActivity::class.java)
                    .putExtra("USER_ID", user.id))
            },
            onDeleteClickListener = { user ->
                presenter.deleteUser(user.id)
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddUser)
            .setOnClickListener {
                startActivity(Intent(this, CreateUserActivity::class.java))
            }

        val model = UserListModel()
        presenter = UserListPresenter(this, model)

        // ✅ NO LLAMES a checkUserAuthentication aquí
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        presenter.loadUsers()
    }

    override fun showUsers(users: List<User>) {
        adapter.updateUsers(users)
    }

    override fun showError(message: String) {
        super.showError(message)
    }
}
