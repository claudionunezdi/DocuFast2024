package com.cnunez.docufast.admin.group.edit.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.create.view.CreateGroupActivity
import com.cnunez.docufast.admin.group.detail.view.GroupDetailActivity
import com.cnunez.docufast.admin.group.edit.contract.ListContract
import com.cnunez.docufast.admin.group.edit.model.ListModel
import com.cnunez.docufast.admin.group.edit.presenter.ListPresenter
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ListActivity : BaseActivity(), ListContract.View {

    private lateinit var presenter: ListContract.Presenter
    private lateinit var adapter: GroupAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list_groups)

        progressBar = findViewById(R.id.progressBar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewGroups)
        adapter = GroupAdapter(emptyList(), object : GroupAdapter.OnItemClickListener {
            override fun onOpenGroupClick(group: Group) {
                val intent = Intent(this@ListActivity, GroupDetailActivity::class.java)
                intent.putExtra("groupId", group.id)
                startActivity(intent)
            }

            override fun onGroupClick(group: Group) {
                val intent = Intent(this@ListActivity, GroupDetailActivity::class.java)
                intent.putExtra("groupId", group.id)
                startActivity(intent)
            }

            override fun onDeleteClick(group: Group) {
                showDeleteConfirmation(group.id)
            }

            override fun onDeleteGroupClick(group: Group) {
                TODO("Not yet implemented")
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddGroup).setOnClickListener {
            startActivity(Intent(this, CreateGroupActivity::class.java))
        }

        val db = FirebaseDatabase.getInstance()
        val model = ListModel(
            userDao = UserDaoRealtime(db),
            groupDao = GroupDaoRealtime(db)
        )
        presenter = ListPresenter(this, model)
    }

    override fun onStart() {
        super.onStart()
        presenter.loadGroups()
    }

    private fun showDeleteConfirmation(groupId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Grupo")
            .setMessage("¿Estás seguro de que quieres eliminar este grupo?")
            .setPositiveButton("Sí") { _, _ -> presenter.deleteGroup(groupId) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        // Ya validado en BaseActivity
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun showGroups(groups: List<Group>) {
        adapter.setGroups(groups)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onOpenGoupClick(group: Group) {
        val intent = Intent(this@ListActivity, GroupDetailActivity::class.java)
        intent.putExtra("groupId", group.id)
        startActivity(intent)
    }

    override fun onGroupClick(group: Group) {
        val intent = Intent(this@ListActivity, GroupDetailActivity::class.java)
        intent.putExtra("groupId", group.id)
        startActivity(intent)
    }

    override fun onDeleteClick(group: Group) {
        showDeleteConfirmation(group.id)
    }



}