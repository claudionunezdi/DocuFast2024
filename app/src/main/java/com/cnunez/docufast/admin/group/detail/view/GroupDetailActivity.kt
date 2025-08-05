package com.cnunez.docufast.admin.group.detail.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.admin.group.detail.model.GroupDetailModel
import com.cnunez.docufast.admin.group.detail.presenter.GroupDetailPresenter
import com.cnunez.docufast.common.adapters.ArchivesAdapter
import com.cnunez.docufast.common.adapters.UserAdapterUnified
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
class GroupDetailActivity : BaseActivity(), GroupDetailContract.View {

    private lateinit var presenter: GroupDetailPresenter
    private lateinit var userAdapter: UserAdapterUnified
    private lateinit var archivesAdapter: ArchivesAdapter
    private lateinit var deleteButton: MaterialButton
    private lateinit var groupNameTv: TextView
    private lateinit var membersCountTv: TextView
    private lateinit var filesCountTv: TextView
    private lateinit var progressBar: View
    private var groupId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        groupId = intent.getStringExtra("groupId") ?: run {
            Toast.makeText(this, "ID de grupo no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupAdapters()
        setupPresenter()
        setupListeners()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        groupNameTv = findViewById(R.id.textViewGroupName)
        membersCountTv = findViewById(R.id.textViewGroupMembersCount)
        filesCountTv = findViewById(R.id.textViewGroupFilesCount)
        deleteButton = findViewById(R.id.buttonDeleteGroup)
    }

    private fun setupAdapters() {
        // Versión CORRECTA usando el builder forView
        userAdapter = UserAdapterUnified.forView(emptyList()) { user ->
            // Manejar clic en usuario si es necesario
            // Ejemplo: abrir detalle de usuario
        }

        archivesAdapter = ArchivesAdapter(mutableListOf()) { file ->
            // Manejar clic en archivo
        }

        findViewById<RecyclerView>(R.id.recyclerViewGroupMembers).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = userAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        findViewById<RecyclerView>(R.id.recyclerViewGroupFiles).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = archivesAdapter
        }
    }

    private fun setupPresenter() {
        val db = FirebaseDatabase.getInstance()
        val model = GroupDetailModel(
            UserDaoRealtime(db),
            FileDaoRealtime(db),
            GroupDaoRealtime(db)
        )
        presenter = GroupDetailPresenter(this, model)
        presenter.loadGroupDetails(groupId)
    }

    private fun setupListeners() {
        deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }

        findViewById<FloatingActionButton>(R.id.fabAddMember)?.setOnClickListener {
            // Navegar a actividad para agregar miembros
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Grupo")
            .setMessage("¿Estás seguro de eliminar este grupo? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                presenter.deleteGroup(groupId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        presenter.checkAdminPermissions(user.uid)
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun showGroupName(name: String) {
        groupNameTv.text = name
    }

    override fun showMembers(users: List<User>) {
        userAdapter.updateUsers(users)
        membersCountTv.text = resources.getQuantityString(
            R.plurals.members_count,
            users.size,
            users.size
        )
    }

    override fun showFiles(files: List<File>) {
        archivesAdapter.setFiles(files)
        filesCountTv.text = resources.getQuantityString(
            R.plurals.files_count,
            files.size,
            files.size
        )
    }

    override fun onGroupDeleted() {
        Toast.makeText(this, "Grupo eliminado exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun setAdminControls(visible: Boolean) {
        deleteButton.isVisible = visible
        findViewById<FloatingActionButton>(R.id.fabAddMember)?.isVisible = visible
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    companion object {
        fun start(context: Context, groupId: String) {
            val intent = Intent(context, GroupDetailActivity::class.java).apply {
                putExtra("groupId", groupId)
            }
            context.startActivity(intent)
        }
    }
}


