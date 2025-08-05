package com.cnunez.docufast.admin.group.members.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.members.contract.AddMembersContract
import com.cnunez.docufast.admin.group.members.model.AddMembersModel
import com.cnunez.docufast.admin.group.members.presenter.AddMembersPresenter
import com.cnunez.docufast.common.adapters.UserAdapterUnified
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.database.FirebaseDatabase

class AddMembersActivity : BaseActivity(), AddMembersContract.View {

    private lateinit var adapter: UserAdapterUnified
    private lateinit var presenter: AddMembersPresenter
    private lateinit var selectedUsers: MutableList<User>
    private var groupId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members)

        groupId = intent.getStringExtra("groupId") ?: run {
            finish()
            return
        }

        setupRecyclerView()
        setupPresenter()
        setupSaveButton()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapterUnified(
            mutableListOf(),
            UserAdapterUnified.Mode.SELECTION,
            object : UserAdapterUnified.OnUserActionListener {
                override fun onUserSelected(user: User, isSelected: Boolean) {
                    if (isSelected) {
                        selectedUsers.add(user)
                    } else {
                        selectedUsers.remove(user)
                    }
                }
                override fun onUserClicked(user: User) {}
                override fun onEditUser(user: User) {}
                override fun onDeleteUser(user: User) {}
            }
        )

        findViewById<RecyclerView>(R.id.recyclerViewUsers).apply {
            layoutManager = LinearLayoutManager(this@AddMembersActivity)
            adapter = this@AddMembersActivity.adapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupPresenter() {
        val db = FirebaseDatabase.getInstance()
        presenter = AddMembersPresenter(
            this,
            AddMembersModel(
                UserDaoRealtime(db),
                GroupDaoRealtime(db)
            )
        )

        SessionManager.getCurrentUser()?.organization?.let {
            presenter.loadAvailableUsers(it, groupId)
        }
    }

    private fun setupSaveButton() {
        findViewById<Button>(R.id.buttonSave).setOnClickListener {
            if (selectedUsers.isNotEmpty()) {
                presenter.addMembersToGroup(groupId, selectedUsers)
            } else {
                Toast.makeText(this, "Selecciona al menos un miembro", LENGTH_SHORT).show()
            }
        }
    }

    override fun showUsers(users: List<User>) {
        adapter.updateUsers(users)
    }

    override fun onMembersAdded() {
        Toast.makeText(this, "Miembros agregados exitosamente", LENGTH_SHORT).show()
        finish()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, LENGTH_LONG).show()
    }

    companion object {
        fun start(context: Context, groupId: String) {
            context.startActivity(Intent(context, AddMembersActivity::class.java).apply {
                putExtra("groupId", groupId)
            })
        }
    }
}