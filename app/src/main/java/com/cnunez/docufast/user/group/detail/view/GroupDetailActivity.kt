package com.cnunez.docufast.user.group.detail.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cnunez.docufast.R
import com.cnunez.docufast.camera.view.CameraActivity
import com.cnunez.docufast.common.adapters.FileAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.cnunez.docufast.databinding.ActivityUserGroupDetailBinding
import com.cnunez.docufast.user.file.detail.view.FileDetailActivity
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.user.group.detail.model.GroupDetailModel
import com.cnunez.docufast.user.group.detail.presenter.GroupDetailPresenter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class GroupDetailActivity : BaseActivity(), GroupDetailContract.View {

    private lateinit var binding: ActivityUserGroupDetailBinding
    private lateinit var presenter: GroupDetailContract.Presenter

    private lateinit var fileAdapter: FileAdapter
    private lateinit var fileDao: FileDaoRealtime
    private lateinit var userDao: UserDaoRealtime

    private lateinit var group: Group
    private lateinit var groupId: String
    private lateinit var organizationId: String
    private lateinit var currentUserId: String

    // Si vuelves de la cámara, recarga lista
    private val cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserGroupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // DAOs
        val db = FirebaseDatabase.getInstance()
        val storageManager = FileStorageManager.getInstance()
        fileDao = FileDaoRealtime(db, storageManager)
        userDao = UserDaoRealtime(db)

        presenter = GroupDetailPresenter(
            view = this,
            model = GroupDetailModel() // tu implementación user
        )

        currentUserId = SessionManager.getCurrentUserId()
            ?: throw IllegalStateException("User ID is null")

        initializeExtras()
        setupUI()
        setupRecyclerView()
        setupFab()
        loadData()
    }

    private fun initializeExtras() {
        val currentOrg = SessionManager.getCurrentOrganization()
            ?: throw IllegalStateException("Organization ID is null")

        group = intent.getParcelableExtra("group")
            ?: throw IllegalArgumentException("Group data is missing")

        organizationId = intent.getStringExtra("organizationId")
            ?: throw IllegalArgumentException("Organization ID is missing")

        groupId = group.id

        // Verifica acceso por organización
        if (currentOrg != organizationId) {
            showError("No tienes acceso a esta organización")
            finish()
        }
    }

    private fun setupUI() {
        supportActionBar?.title = group.name
        supportActionBar?.subtitle = group.description
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            files = emptyList(),
            onOpenClick = { file -> presenter.onFileSelected(file) },
            onEditClick = { file -> onEditClick(file) },
            onViewContentClick = { file -> presenter.onFileSelected(file) }
        )
        binding.recyclerViewFiles.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = fileAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabAddFile.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java).apply {
                putExtra("groupId", groupId)
                putExtra("organizationId", organizationId)
                putExtra("userId", currentUserId)
            }
            cameraActivityLauncher.launch(intent)
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                // carga inicial rápida desde DAO
                val files = fileDao.getFilesByGroup(groupId)
                fileAdapter.updateFiles(files)
                // suscríbete a actualizaciones
                presenter.observeGroupFiles(groupId, organizationId, null)
            } catch (e: Exception) {
                showError("Error al cargar archivos: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun onEditClick(file: File) {
        // En vista usuario, solo permite si es dueño o admin
        if (file.metadata.createdBy == currentUserId || SessionManager.isAdmin()) {
            Toast.makeText(this, "Editando: ${file.name}", Toast.LENGTH_SHORT).show()
            // tu flujo de edición aquí
        } else {
            showError("Solo el creador puede editar este archivo")
        }
    }

    // ===== Implementación de la interfaz View =====

    override fun showFiles(files: List<File>, filterType: FileType?) {
        lifecycleScope.launch {
            try {
                // Enriquecer con nombre del creador
                val enriched = files.map { file ->
                    val creatorName = userDao.getById(file.metadata.createdBy)?.name
                        ?: "Usuario ${file.metadata.createdBy.take(6)}"
                    when (file) {
                        is File.ImageFile -> file.copy(metadata = file.metadata.copy(creatorName = creatorName))
                        is File.TextFile -> file.copy(metadata = file.metadata.copy(creatorName = creatorName))
                        is File.OcrResultFile -> file.copy(metadata = file.metadata.copy(creatorName = creatorName))
                        else -> file
                    }
                }
                fileAdapter.updateFiles(enriched)
            } catch (e: Exception) {
                showError("Error al enriquecer archivos: ${e.message}")
            }
        }
    }

    override fun showFileDetail(file: File) {
        // Si el archivo está protegido por reglas de Storage, valida metadatos antes
        if (file.storageInfo.path.isNotEmpty()) {
            FirebaseStorage.getInstance()
                .getReference(file.storageInfo.path)
                .metadata
                .addOnSuccessListener { startFileDetail(file) }
                .addOnFailureListener { showError("No tienes permiso para ver este archivo") }
        } else {
            startFileDetail(file)
        }
    }

    private fun startFileDetail(file: File) {
        val intent = Intent(this, FileDetailActivity::class.java).apply {
            putExtra("file", file)
            putExtra("organizationId", organizationId)
            file.storageInfo.downloadUrl?.let { putExtra("downloadUrl", it) }
        }
        startActivity(intent)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Progreso opcional (si tu contrato los define)


    // Estos dos son NO-OP en la versión usuario (por si el contrato común los exige)
    override fun showUserPicker(users: List<User>) {
        // No aplica en vista usuario; si algún día habilitas, aquí va el diálogo.
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.feature_not_available))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }


    override fun onDestroy() {
        (presenter as? GroupDetailPresenter)?.onDestroy()
        super.onDestroy()
    }
}
