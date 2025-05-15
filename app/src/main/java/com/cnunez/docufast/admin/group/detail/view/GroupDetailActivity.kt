package com.cnunez.docufast.admin.group.detail.view

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.detail.model.GroupDetailModel
import com.cnunez.docufast.common.adapters.ArchivesAdapter
import com.cnunez.docufast.common.adapters.UserAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.Group
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailActivity :BaseActivity(), GroupDetailContract.View {

    private lateinit var presenter: GroupDetailContract.Presenter
    private lateinit var textViewGroupName: TextView
    private lateinit var textViewGroupMembersCount: TextView
    private lateinit var recyclerViewGroupMembers: RecyclerView
    private lateinit var recyclerViewGroupFiles: RecyclerView
    private lateinit var archivesAdapter: ArchivesAdapter
    private lateinit var userAdapter: UserAdapter
    private lateinit var buttonDeleteGroup: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        textViewGroupName = findViewById(R.id.textViewGroupName)
        textViewGroupMembersCount = findViewById(R.id.textViewGroupMembersCount)
        recyclerViewGroupMembers = findViewById(R.id.recyclerViewGroupMembers)
        recyclerViewGroupFiles = findViewById(R.id.recyclerViewGroupFiles)
        buttonDeleteGroup = findViewById(R.id.buttonDeleteGroup)

        archivesAdapter = ArchivesAdapter(mutableListOf())
        userAdapter = UserAdapter(mutableListOf())
        recyclerViewGroupFiles.layoutManager = LinearLayoutManager(this)
        recyclerViewGroupFiles.adapter = archivesAdapter
        recyclerViewGroupMembers.layoutManager = LinearLayoutManager(this)
        recyclerViewGroupMembers.adapter = userAdapter

        presenter = GroupDetailPresenter(this, GroupDetailModel(this))

        val groupId = intent.getStringExtra("groupId")
        if (groupId != null) {
            presenter.loadGroupDetails(groupId)
        } else {
            Toast.makeText(this, "Invalid group ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        buttonDeleteGroup.setOnClickListener {
            groupId?.let { id ->
                showDeleteConfirmationDialog(id)
            } ?: Toast.makeText(this, "Invalid group ID", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onUserAuthenticated(user: FirebaseUser) {
        if (user.isAnonymous) {
            showError("User is not authenticated")
            finish()
        } else {
            val groupId = intent.getStringExtra("groupId")
            if (groupId.isNullOrEmpty()) {
                showError("Invalid group ID")
            } else {
                presenter.loadGroupDetails(groupId)
            }
        }
    }

    private fun showDeleteConfirmationDialog(groupId: String) {
        AlertDialog.Builder(this)
            .setMessage("¿Seguro que quieres eliminar el grupo?")
            .setPositiveButton("Sí") { _, _ ->
                deleteGroup(groupId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteGroup(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups").document(groupId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Grupo eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al eliminar el grupo: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun showGroupDetails(group: Group) {
        textViewGroupName.text = group.name
        "Members: ${group.members.size}".also { textViewGroupMembersCount.text = it }
        archivesAdapter.setFiles(group.files)
        userAdapter.setUsers(group.members)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}