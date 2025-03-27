package com.cnunez.docufast.admin.group.edit.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.create.view.CreateGroupActivity
import com.cnunez.docufast.admin.group.edit.contract.ListContract
import com.cnunez.docufast.admin.group.edit.model.ListModel
import com.cnunez.docufast.admin.group.edit.presenter.ListPresenter
import com.cnunez.docufast.common.adapters.GroupListAdapter
import com.cnunez.docufast.common.dataclass.Group
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListActivity : AppCompatActivity(), ListContract.View {

    private lateinit var recyclerViewGroups: RecyclerView
    private lateinit var groupListAdapter: GroupListAdapter
    private lateinit var searchView: SearchView
    private lateinit var fabAddGroup: FloatingActionButton
    private lateinit var presenter: ListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list_groups)

        presenter = ListPresenter(this, ListModel(this))
        initializeUI()
        presenter.loadGroups()
    }

    private fun initializeUI() {
        recyclerViewGroups = findViewById(R.id.recyclerViewGroups)
        recyclerViewGroups.layoutManager = LinearLayoutManager(this)
        groupListAdapter = GroupListAdapter(mutableListOf())
        recyclerViewGroups.adapter = groupListAdapter

        fabAddGroup = findViewById(R.id.fabAddGroup)
        fabAddGroup.setOnClickListener {
            startActivityForResult(
                Intent(this, CreateGroupActivity::class.java),
                REQUEST_CODE_CREATE_GROUP
            )
        }

        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {

                // Implement search functionality if needed
                return true
            }
        })
    }

    override fun showGroups(groups: List<Group>) {
        groupListAdapter.setGroups(groups)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CREATE_GROUP && resultCode == RESULT_OK) {
            presenter.loadGroups()
        }
    }

    companion object {
        private const val REQUEST_CODE_CREATE_GROUP = 1001
    }
}