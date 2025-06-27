package com.cnunez.docufast.admin.group.detail.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.admin.group.detail.model.GroupDetailModel
import com.cnunez.docufast.admin.group.detail.presenter.GroupDetailPresenter
import com.cnunez.docufast.common.adapters.ArchivesAdapter
import com.cnunez.docufast.common.adapters.UserAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class GroupDetailActivity : BaseActivity(), GroupDetailContract.View {

    private lateinit var presenter: GroupDetailContract.Presenter
    private lateinit var userAdapter: UserAdapter
    private lateinit var archivesAdapter: ArchivesAdapter
    private lateinit var deleteButton: MaterialButton
    private lateinit var groupNameTv: TextView
    private lateinit var progressBar: View

    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        // Obtener el ID del grupo
        groupId = intent.getStringExtra("groupId").orEmpty()
        if (groupId.isBlank()) {
            showError("ID de grupo inválido")
            finish()
            return
        }

        // Inicializar vistas
        userAdapter     = UserAdapter(emptyList())
        archivesAdapter = ArchivesAdapter(emptyList())
        progressBar     = findViewById(R.id.progressBar)
        groupNameTv     = findViewById(R.id.textViewGroupName)
        deleteButton    = findViewById(R.id.buttonDeleteGroup)

        findViewById<RecyclerView>(R.id.recyclerViewGroupMembers).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = userAdapter
        }

        findViewById<RecyclerView>(R.id.recyclerViewGroupFiles).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = archivesAdapter
        }

        // Crear el model y presenter
        val db = FirebaseDatabase.getInstance()
        val model = GroupDetailModel(
            UserDaoRealtime(db),
            FileDaoRealtime(db),
            GroupDaoRealtime(db)
        )
        presenter = GroupDetailPresenter(this, model)

        // Botón de eliminar grupo
        deleteButton.setOnClickListener {
            presenter.deleteGroup(groupId)
        }

        // Cargar detalles
        presenter.loadGroupDetails(groupId)
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        // Validar rol para mostrar botón de eliminación
        FirebaseDatabase.getInstance()
            .getReference("users").child(user.uid).child("role")
            .get().addOnSuccessListener { snap ->
                deleteButton.isVisible = snap.getValue(String::class.java) == "ADMIN"
            }.addOnFailureListener {
                deleteButton.isVisible = false
            }

        // Cargar nombre del grupo
        FirebaseDatabase.getInstance()
            .getReference("groups").child(groupId).child("name")
            .get().addOnSuccessListener { snap ->
                snap.getValue(String::class.java)?.let { showGroupName(it) }
            }
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
        userAdapter.setUsers(users)
    }

    override fun showFiles(files: List<File>) {
        archivesAdapter.setFiles(files)
    }

    override fun onGroupDeleted() {
        Toast.makeText(this, "Grupo eliminado", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
