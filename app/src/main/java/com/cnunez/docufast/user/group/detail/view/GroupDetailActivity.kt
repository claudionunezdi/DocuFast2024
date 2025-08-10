package com.cnunez.docufast.user.group.detail.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.camera.view.CameraActivity
import com.cnunez.docufast.common.adapters.FileAdapter
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.databinding.ActivityUserGroupDetailBinding
import com.cnunez.docufast.user.file.detail.view.FileDetailActivity
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.user.group.detail.model.GroupDetailModel
import com.cnunez.docufast.user.group.detail.presenter.GroupDetailPresenter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

class GroupDetailActivity : AppCompatActivity(), GroupDetailContract.View {

    private lateinit var binding: ActivityUserGroupDetailBinding
    private lateinit var presenter: GroupDetailContract.Presenter
    private lateinit var fileAdapter: FileAdapter
    private lateinit var group: Group
    private lateinit var organizationId: String
    private lateinit var currentUserId: String

    private val cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            presenter.loadGroupFiles(group.id, organizationId, FileType.TEXT)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserGroupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = GroupDetailPresenter(this, GroupDetailModel())
        initializeExtras()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUserId = currentUser?.uid ?: run {
            showError("Usuario no autenticado")
            finish()
            return
        }
        loadData()
        setupUI()



    }





    private fun initializeExtras() {
        group = intent.getParcelableExtra("group") ?: throw IllegalArgumentException("Group data is missing")
        organizationId = intent.getStringExtra("organizationId") ?: throw IllegalArgumentException("Organization ID is missing")
    }


    private fun setupUI() {
        setupRecyclerView()
        setupFab()
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            files = emptyList(),
            onOpenClick = { file -> handleFileClick(file) },
            onEditClick = {  },
            onViewContentClick = { /* Implementar si es necesario */ },
            showCreationDate = true
        )

        binding.recyclerViewFiles.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = fileAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleFileClick(file: File) {
        if (file.storageInfo.path.isNotEmpty()) {
            checkFilePermission(file)
        } else {
            presenter.onFileSelected(file)
        }
    }

    private fun setupFab() {
        binding.fabAddFile.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java).apply {
                putExtra("groupId", group.id)
                putExtra("organizationId", organizationId)
                putExtra("userId", currentUserId) // Añadir esta línea
            }
            cameraActivityLauncher.launch(intent)
        }
    }

    private fun loadData() {
        // Reemplazar loadGroupFiles por:
        (presenter as GroupDetailPresenter).observeGroupFiles(group.id, organizationId, FileType.TEXT)
    }

    private fun checkFilePermission(file: File) {
        FirebaseStorage.getInstance().getReference(file.storageInfo.path).metadata
            .addOnSuccessListener {
                presenter.onFileSelected(file)
            }
            .addOnFailureListener { e ->
                showError("No tienes permiso para ver este archivo")
            }
    }

    override fun showFiles(files: List<File>, filterType: FileType?) {
        fileAdapter.setFiles(files)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showFileDetail(file: File) {
        val intent = Intent(this, FileDetailActivity::class.java).apply {
            putExtra("file", file)
            putExtra("organizationId", file.metadata.organizationId)
            file.storageInfo.downloadUrl?.let { putExtra("downloadUrl", it) }
        }
        startActivity(intent)
    }



    override fun onDestroy() {
        (presenter as? GroupDetailPresenter)?.onDestroy()
        super.onDestroy()
    }
}