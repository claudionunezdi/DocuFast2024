package com.cnunez.docufast.admin.group.detail.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.admin.group.detail.model.GroupDetailModel
import com.cnunez.docufast.admin.group.detail.presenter.GroupDetailPresenter
import com.cnunez.docufast.admin.group.fileContent.view.FileContentActivity
import com.cnunez.docufast.common.adapters.FileAdapter
import com.cnunez.docufast.common.adapters.UserAdapterUnified
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailActivity : BaseActivity(), GroupDetailContract.View {

    private lateinit var presenter: GroupDetailPresenter
    private lateinit var userAdapter: UserAdapterUnified
    private lateinit var fileAdapter: FileAdapter
    private lateinit var deleteButton: MaterialButton
    private lateinit var groupNameTv: TextView
    private lateinit var membersCountTv: TextView
    private lateinit var filesCountTv: TextView
    private lateinit var progressBar: View
    private var groupId: String = ""
    private lateinit var progressBarFiles: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        initializeGroupId()
        initializeViews()
        setupAdapters()
        setupPresenter()
        setupListeners()
        setupFileAdapter()
        loadGroupData()
    }

    private fun initializeGroupId() {
        groupId = intent.getStringExtra("groupId") ?: run {
            Toast.makeText(this, "ID de grupo no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        groupNameTv = findViewById(R.id.textViewGroupName)
        membersCountTv = findViewById(R.id.textViewGroupMembersCount)
        filesCountTv = findViewById(R.id.textViewGroupFilesCount)
        deleteButton = findViewById(R.id.buttonDeleteGroup)
    }

    private fun setupAdapters() {
        // Configuración del adaptador de usuarios
        userAdapter = UserAdapterUnified.forView(
            users = emptyList(),
            onClick = { user -> showUserDetails(user) },
            onDeleteClick = { user -> showDeleteMemberConfirmation(user) }
        )

        // Configuración del adaptador de archivos
        fileAdapter = FileAdapter(
            files = emptyList(),
            onOpenClick = { file -> handleFileOpen(file) },
            onEditClick = { file -> handleFileEdit(file) },
            onViewContentClick = { file -> handleFileContent(file) },  // <-- Este es el importante
            showCreationDate = true
        )

        findViewById<RecyclerView>(R.id.recyclerViewGroupFiles).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = fileAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        // Configuración del RecyclerView de miembros
        findViewById<RecyclerView>(R.id.recyclerViewGroupMembers).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = userAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        // Configuración del RecyclerView de archivos
        findViewById<RecyclerView>(R.id.recyclerViewGroupFiles).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = fileAdapter
        }
    }

    private fun setupPresenter() {
        val db = FirebaseDatabase.getInstance()
        val storageManager = FileStorageManager.getInstance()
        val model = GroupDetailModel(
            UserDaoRealtime(db),
            FileDaoRealtime(db, storageManager),
            GroupDaoRealtime(db)
        )
        presenter = GroupDetailPresenter(this, model)
        presenter.loadGroupDetails(groupId)
    }

    private fun setupListeners() {
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        findViewById<FloatingActionButton>(R.id.fabAddMember)?.setOnClickListener {
            navigateToAddMembers()
        }
    }

    // Métodos para manejar interacciones con archivos
    private fun handleFileOpen(file: File) {
        when (file) {
            is File.TextFile, is File.OcrResultFile -> {
                FileContentActivity.start(this, file.id)
            }
            is File.ImageFile -> {
                // Opcional: Abrir visor de imágenes si es solo una imagen
                openImageViewer(file)
            }
            else -> {
                Toast.makeText(this, "Tipo de archivo no soportado", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openImageViewer(imageFile: File.ImageFile) {
        Toast.makeText(this, "Hola estoy abriendo", Toast.LENGTH_SHORT).show()
        // Implementar lógica para abrir visor de imágenes fullscreen
        Toast.makeText(this, "Abriendo imagen: ${imageFile.name}", Toast.LENGTH_SHORT).show()
    }

    private fun handleFileEdit(file: File) {
        Toast.makeText(this, "Editando archivo: ${file.name}", Toast.LENGTH_SHORT).show()
        // Implementar lógica para editar archivo
    }

    private fun handleFileContent(file: File) {
        when (file) {
            is File.TextFile -> {
                FileContentActivity.start(this, file.id)
            }
            is File.OcrResultFile -> {
                FileContentActivity.start(this, file.id)
            }
            is File.ImageFile -> {
                if (file.linkedOcrTextId != null) {
                    // Si tiene texto OCR asociado, abrir el visor de contenido
                    FileContentActivity.start(this, file.linkedOcrTextId)
                } else {
                    // Si es solo imagen, abrir visor de imágenes
                    openImageViewer(file)
                }
            }
            else -> {
                Toast.makeText(this, "Tipo de archivo no soportado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUserDetails(user: User) {
        Toast.makeText(this, "Mostrando detalles de ${user.name}", Toast.LENGTH_SHORT).show()
        // Implementar navegación a detalles de usuario
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_group_title))
            .setMessage(getString(R.string.delete_group_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                presenter.deleteGroup(groupId)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun navigateToAddMembers() {
        // Implementar navegación a pantalla de agregar miembros
        Toast.makeText(this, "Agregar miembros", Toast.LENGTH_SHORT).show()
    }

    // Implementación de GroupDetailContract.View
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
        fileAdapter.setFiles(files)
        filesCountTv.text = resources.getQuantityString(
            R.plurals.files_count,
            files.size,
            files.size
        )
    }

    override fun onGroupDeleted() {
        Toast.makeText(this, getString(R.string.group_deleted_success), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun setAdminControls(visible: Boolean) {
        deleteButton.isVisible = visible
        findViewById<FloatingActionButton>(R.id.fabAddMember)?.isVisible = visible
    }

    private fun showDeleteMemberConfirmation(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar miembro del grupo")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.name} de este grupo?")
            .setPositiveButton("Eliminar") { _, _ ->
                presenter.removeMemberFromGroup(groupId, user.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onMemberRemoved(userId: String) {
        Toast.makeText(this, "Miembro eliminado del grupo", Toast.LENGTH_SHORT).show()
        // No necesitas recargar manualmente aquí, ya que el Presenter lo hace
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    private fun setupFileAdapter() {
        fileAdapter = FileAdapter(
            files = emptyList(),
            onOpenClick = { file -> openFile(file) },
            onEditClick = { file -> editFile(file) },
            onViewContentClick = { file -> viewFileContent(file) },
            showCreationDate = true // Mostrar fecha de creación
        )

        findViewById<RecyclerView>(R.id.recyclerViewGroupFiles).apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = fileAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun loadGroupData() {

        presenter.loadGroupFiles(groupId)
    }

    // Implementación de los métodos de View para archivos


    override fun showFileLoadingProgress() {
        // Mostrar progreso específico para archivos
        findViewById<ProgressBar>(R.id.progressBarFiles)?.visibility = View.VISIBLE
    }

    override fun hideFileLoadingProgress() {
        findViewById<ProgressBar>(R.id.progressBarFiles)?.visibility = View.GONE
    }

    override fun showFileError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateFilesCount(count: Int) {
        findViewById<TextView>(R.id.textViewGroupFilesCount)?.text =
            resources.getQuantityString(R.plurals.files_count, count, count)
    }

    // Métodos para manejar acciones de archivos
    private fun openFile(file: File) {
        handleFileContent(file)
    }

    private fun openImageFile(imageFile: File.ImageFile) {
        handleFileContent(imageFile)
    }

    private fun openTextFile(textFile: File.TextFile) {
        handleFileContent(textFile)
    }



    private fun editFile(file: File) {
        // Implementar edición de archivo
        Toast.makeText(this, "Editando: ${file.name}", Toast.LENGTH_SHORT).show()
    }

    private fun viewFileContent(file: File) {
        handleFileContent(file)
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