package com.cnunez.docufast.admin.group.list.view

import android.annotation.SuppressLint
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
import com.cnunez.docufast.admin.group.list.contract.ListContract
import com.cnunez.docufast.admin.group.list.model.ListModel
import com.cnunez.docufast.admin.group.list.presenter.ListPresenter
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.cnunez.docufast.common.manager.GroupManager
import com.cnunez.docufast.common.firebase.storage.FileStorageManager

class ListActivity : BaseActivity(), ListContract.View {

    private lateinit var presenter: ListContract.Presenter
    private lateinit var adapter: GroupAdapter
    private lateinit var progressBar: ProgressBar
    private val groupManager by lazy { GroupManager(FileStorageManager.getInstance()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager.getCurrentUser()?.role != "ADMIN") {
            Toast.makeText(this, "Acceso solo para administradores", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_admin_list_groups)
        progressBar = findViewById(R.id.progressBar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewGroups)
        adapter = GroupAdapter(
            groups = emptyList(),
            listener = object : GroupAdapter.OnItemClickListener {
                override fun onOpenGroupClick(group: Group) { openGroupDetail(group.id) }
                override fun onGroupClick(group: Group)     { openGroupDetail(group.id) }
                override fun onDeleteClick(group: Group)    { showDeleteConfirmation(group.id) }

                // Si tu adapter define esto adicional, mejor NO borrar aquí
                @SuppressLint("NotifyDataSetChanged")
                override fun onDeleteGroupClick(group: Group) {
                    showDeleteConfirmation(group.id)
                    // NO: presenter.deleteGroup(...) + remove + finish
                    // Dejamos que el Presenter borre y luego recargamos la lista
                }
            },
            groupManager = groupManager // si tu constructor lo permite; si es obligatorio, pásale uno dummy
        )

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

    override fun onResume() {
        super.onResume()
        presenter.loadGroups() // una sola entrada a la pantalla, recarga
    }

    private fun openGroupDetail(groupId: String) {
        val intent = Intent(this, GroupDetailActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivity(intent)
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

    override fun showProgress()   { progressBar.visibility = View.VISIBLE }
    override fun hideProgress()   { progressBar.visibility = View.GONE }

    override fun showGroups(groups: List<Group>) {
        runOnUiThread { adapter.setGroups(groups) }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onOpenGoupClick(group: Group) { // <- si no usas este, puedes eliminarlo o redirigir
        openGroupDetail(group.id)
    }

    override fun onGroupClick(group: Group) {
        openGroupDetail(group.id)
    }

    override fun onDeleteClick(group: Group) {
        showDeleteConfirmation(group.id)
    }
}
