package com.cnunez.docufast.user.file.detail.view

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.databinding.ActivityFileContentBinding
import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.cnunez.docufast.user.file.detail.presenter.FileDetailPresenter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class FileDetailActivity : BaseActivity(), FileDetailContract.View {

    private lateinit var presenter: FileDetailContract.Presenter
    private lateinit var binding: ActivityFileContentBinding
    private lateinit var currentFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityFileContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar datos del intent
        val fileId = intent.getStringExtra("fileId") ?: return
        val organizationId = intent.getStringExtra("organizationId") ?: return
        val imageUri = intent.getParcelableExtra<Uri>("imageUri")

        currentFile = intent.getParcelableExtra("file")
            ?: throw IllegalArgumentException("File data is missing")

        // Configurar UI según el tipo de archivo
        setupUIForFile(currentFile)

        // Inicializar presenter
        presenter = FileDetailPresenter(this)
        presenter.loadFileContent(fileId, organizationId)

        // Si vino una imagen directa
        imageUri?.let {
            binding.imageViewPreview.setImageURI(it)
        }

        // Guardar cambios en texto
        binding.saveButton.setOnClickListener {
            val newContent = binding.fileContentEditText.text.toString()
            val updatedFile = (currentFile as? File.TextFile)?.copy(content = newContent)
            if (updatedFile != null) {
                presenter.saveFileContent(updatedFile)
            }
        }

        // Botón volver
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        if (user.isAnonymous) {
            Toast.makeText(this, "Anonymous user detected. Redirecting to login.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Welcome ${user.email}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUIForFile(file: File) {
        when (file) {
            is File.TextFile -> {
                binding.imageViewPreview.visibility = View.GONE
                binding.fileContentEditText.setText(file.content)
                binding.fileContentEditText.isEnabled = true
                binding.saveButton.visibility = View.VISIBLE
            }
            is File.ImageFile -> {
                binding.imageViewPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(file.storageInfo.downloadUrl)
                    .into(binding.imageViewPreview)
                binding.saveButton.visibility = View.GONE
                if (!file.linkedOcrTextId.isNullOrEmpty()) {
                    loadOcrText(file.linkedOcrTextId)
                } else {
                    binding.fileContentEditText.visibility = View.GONE
                }
            }
            is File.OcrResultFile -> {
                binding.imageViewPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(file.originalImage.downloadUrl)
                    .into(binding.imageViewPreview)
                binding.fileContentEditText.setText(file.extractedText)
                binding.saveButton.visibility = View.GONE
            }
            else -> {
                showError("Tipo de archivo no soportado para vista previa")
            }
        }
    }

    private fun loadOcrText(ocrTextId: String) {
        FirebaseDatabase.getInstance()
            .getReference("files")
            .child(ocrTextId)
            .get()
            .addOnSuccessListener { snapshot ->
                val textFile = snapshot.getValue(File.TextFile::class.java)
                textFile?.let {
                    binding.fileContentEditText.visibility = View.VISIBLE
                    binding.fileContentEditText.setText(it.content)
                }
            }
    }

    override fun showFileContent(file: File.TextFile) {
        binding.fileContentEditText.setText(file.content)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
