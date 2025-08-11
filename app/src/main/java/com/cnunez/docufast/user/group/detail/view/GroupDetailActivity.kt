package com.cnunez.docufast.user.group.detail.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
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

    private var group: Group? = null
    private lateinit var groupId: String
    private lateinit var organizationId: String
    private lateinit var currentUserId: String

    // Vuelve de cámara → recarga
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) loadData()
    }

    // Vuelve del detalle (guardado) → recarga
    private val fileDetailLauncher = registerForActivityResult(
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

        presenter = GroupDetailPresenter(this, GroupDetailModel())

        // Sesión
        currentUserId = SessionManager.getCurrentUserId()
            ?: run { showError("Sesión inválida"); finish(); return }

        // Extras
        val currentOrg = SessionManager.getCurrentOrganization()
            ?: run { showError("Organización inválida"); finish(); return }

        group = intent.getParcelableExtra("group")
            ?: run { showError("Faltan datos del grupo"); finish(); return }

        organizationId = intent.getStringExtra("organizationId")
            ?: run { showError("Falta organizationId"); finish(); return }

        if (currentOrg != organizationId) {
            showError("No tienes acceso a esta organización")
            finish()
            return
        }

        groupId = group!!.id

        setupUI()
        setupRecyclerView()
        setupFab()
        loadData()
    }

    private fun setupUI() {
        supportActionBar?.title = group?.name.orEmpty()
        supportActionBar?.subtitle = group?.description.orEmpty()
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
            val i = Intent(this, CameraActivity::class.java).apply {
                putExtra("groupId", groupId)
                putExtra("organizationId", organizationId)
                putExtra("userId", currentUserId)
            }
            cameraLauncher.launch(i)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                setLoading(true)
                // 1) Carga del DAO
                val files = fileDao.getFilesByGroup(groupId)
                // 2) Fusión imagen + texto (muestra un solo ítem OCR_RESULT)
                val merged = mergeImageAndText(files)
                // 3) Pintar
                fileAdapter.updateFiles(merged)
                // 4) Suscripción a cambios (si tu Presenter lo hace)
                presenter.observeGroupFiles(groupId, organizationId, null)
            } catch (e: Exception) {
                showError("Error al cargar archivos: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun onEditClick(file: File) {
        if (file.metadata.createdBy == currentUserId || SessionManager.isAdmin()) {
            Toast.makeText(this, "Editando: ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            showError("Solo el creador puede editar este archivo")
        }
    }

    // ===== GroupDetailContract.View =====

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

                val merged = mergeImageAndText(enriched)

                val finalList = when (filterType) {
                    FileType.IMAGE      -> merged.filter { it.type == FileType.IMAGE }
                    FileType.TEXT       -> merged.filter { it.type == FileType.TEXT }
                    FileType.OCR_RESULT -> merged.filter { it.type == FileType.OCR_RESULT }
                    else                -> merged
                }

                fileAdapter.updateFiles(finalList)
            } catch (e: Exception) {
                showError("Error al procesar archivos: ${e.message}")
            }
        }
    }

    override fun showFileDetail(file: File) {
        // TextFile -> abrir directo
        if (file is File.TextFile) {
            startFileDetail(file)
            return
        }

        // Otros tipos: si hay path, valida metadata; si 404 abrimos igual
        val path = file.storageInfo.path
        if (path.isBlank()) {
            startFileDetail(file)
            return
        }

        FirebaseStorage.getInstance()
            .getReference(path)
            .metadata
            .addOnSuccessListener { startFileDetail(file) }
            .addOnFailureListener { e ->
                val msg = e.message.orEmpty()
                if (msg.contains("Object does not exist", ignoreCase = true)) {
                    startFileDetail(file) // 404 -> abrir igual
                } else {
                    showError("No tienes permiso para ver este archivo")
                }
            }
    }

    private fun startFileDetail(file: File) {
        val i = Intent(this, FileDetailActivity::class.java).apply {
            putExtra("file", file)
            putExtra("fileId", file.id)
            putExtra("organizationId", organizationId)
            putExtra("groupId", groupId) // ← importante para RTDB por grupo
            file.storageInfo.downloadUrl.takeIf { it.isNotBlank() }?.let {
                putExtra("downloadUrl", it)
            }
        }
        fileDetailLauncher.launch(i) // ← usamos launcher, no startActivity
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showUserPicker(users: List<User>) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.feature_not_available))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onDestroy() {
        (presenter as? GroupDetailPresenter)?.onDestroy()
        super.onDestroy()
    }

    // ---------- Une imagen + texto OCR en un solo ítem ----------
    private fun mergeImageAndText(files: List<File>): List<File> {
        val byId = files.associateBy { it.id }
        val usados = mutableSetOf<String>()
        val result = mutableListOf<File>()

        // A) Imagen -> Texto por linkedOcrTextId
        files.forEach { f ->
            if (f is File.ImageFile && !f.linkedOcrTextId.isNullOrBlank()) {
                val text = byId[f.linkedOcrTextId!!] as? File.TextFile
                if (text != null) {
                    usados += f.id
                    usados += text.id
                    result += File.OcrResultFile(
                        id = f.id,
                        name = f.name,
                        metadata = f.metadata,
                        storageInfo = f.storageInfo,
                        originalImage = File.OcrResultFile.ImageReference(
                            imageId = f.id,
                            downloadUrl = f.storageInfo.downloadUrl
                        ),
                        extractedText = text.content,
                        confidence = text.ocrData?.confidence ?: 0f
                    )
                }
            }
        }

        // B) Texto -> Imagen por sourceImageId
        files.forEach { f ->
            if (f is File.TextFile && !f.sourceImageId.isNullOrBlank() && f.id !in usados) {
                val img = byId[f.sourceImageId!!] as? File.ImageFile
                if (img != null && img.id !in usados) {
                    usados += img.id
                    usados += f.id
                    result += File.OcrResultFile(
                        id = img.id,
                        name = img.name,
                        metadata = img.metadata,
                        storageInfo = img.storageInfo,
                        originalImage = File.OcrResultFile.ImageReference(
                            imageId = img.id,
                            downloadUrl = img.storageInfo.downloadUrl
                        ),
                        extractedText = f.content,
                        confidence = f.ocrData?.confidence ?: 0f
                    )
                }
            }
        }

        // C) Dejar los no fusionados
        files.forEach { f ->
            if (f.id !in usados) result += f
        }

        return result
    }
}
