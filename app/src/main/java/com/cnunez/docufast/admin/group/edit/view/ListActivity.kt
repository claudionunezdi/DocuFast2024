package com.cnunez.docufast.admin.group.edit.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.SearchView
import com.cnunez.docufast.adapters.GroupAdapter
import com.cnunez.docufast.admin.group.create.view.CreateGroupActivity
import com.cnunez.docufast.admin.group.detail.view.GroupDetailActivity

class ListActivity : AppCompatActivity(), GroupAdapter.OnItemClickListener {

    private lateinit var recyclerViewGroups: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var searchView: SearchView
    private var hasAdminPermissions: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list_groups)

        if (savedInstanceState != null) {
            hasAdminPermissions = savedInstanceState.getBoolean("hasAdminPermissions", false)
        } else {
            checkAdminPermissions()
        }

        initializeUI()
    }

    private fun checkAdminPermissions() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userRef = db.collection("users").document(currentUser.uid)
            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")?.lowercase()

                    if (role == "admin") {
                        hasAdminPermissions = true
                        Log.d("ListActivity", "User has admin permissions")
                    } else {
                        Toast.makeText(this, "User does not have admin permissions", Toast.LENGTH_SHORT).show()
                        Log.d("ListActivity", "User does not have admin permissions")
                    }
                } else {
                    Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show()
                    Log.d("ListActivity", "User document not found")
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error getting user document: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("ListActivity", "Error getting user document: ${exception.message}")
            }
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
            Log.d("ListActivity", "No authenticated user found.")
        }
    }

    private fun initializeUI() {
        recyclerViewGroups = findViewById(R.id.recyclerViewGroups)
        recyclerViewGroups.layoutManager = LinearLayoutManager(this)
        groupAdapter = GroupAdapter(emptyList(), this)
        recyclerViewGroups.adapter = groupAdapter

        val fabAddGroup: FloatingActionButton = findViewById(R.id.fabAddGroup)
        fabAddGroup.setOnClickListener {
            if (hasAdminPermissions) {
                val intent = Intent(this, CreateGroupActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Not have permissions enough to create a group",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("ListActivity", "User tried to create a group without permissions.")
            }
        }

        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                groupAdapter.filter.filter(newText ?: "")
                return true
            }
        })

        loadGroups()
    }

    private fun loadGroups() {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .get()
            .addOnSuccessListener { result ->
                val groups = result.map { document -> document.toObject(WorkGroup::class.java) }
                groupAdapter.setGroups(groups)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting groups: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.d("ListActivity", "Error getting groups: ${exception.message}")
            }
    }

    override fun onItemClick(group: WorkGroup) {
        val intent = Intent(this, GroupDetailActivity::class.java)
        intent.putExtra("groupId", group.id)
        startActivity(intent)
    }
}