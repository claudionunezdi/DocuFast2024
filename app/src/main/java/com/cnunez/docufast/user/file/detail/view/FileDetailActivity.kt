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
        binding = ActivityFileContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Recupera el File y extras
        currentFile = intent.getParcelableExtra("file")
            ?: throw IllegalArgumentException("File data is missing")

        val fileId = intent.getStringExtra("fileId") ?: currentFile.id
        val organizationId = intent.getStringExtra("organizationId")
            ?: currentFile.metadata.organizationId
        val groupId = intent.getStringExtra("groupId") // puede ser null si no usas rutas por grupo

        // 2) Presenter
        presenter = FileDetailPresenter(this)

        // 3) Configura la UI según el tipo de archivo
        setupUIForFile(currentFile)

        // 4) Si es TextFile y viene sin contenido, lo cargamos desde RTDB
        if (currentFile is File.TextFile) {
            val tf = currentFile as File.TextFile
            if (tf.content.isBlank()) {
                presenter.loadFileContent(fileId, organizationId)
            }
        }

        // 5) Imagen directa opcional (si alguien te pasó imageUri)
        intent.getParcelableExtra<Uri>("imageUri")?.let {
            binding.imageViewPreview.setImageURI(it)
        }

        // 6) Guardar
        binding.saveButton.setOnClickListener {
            binding.saveButton.isEnabled = false
            val newContent = binding.fileContentEditText.text.toString()

            when (val f = currentFile) {
                is File.TextFile -> {
                    // guardamos el TextFile (RTDB + Storage si tu Presenter/DAO lo hace)
                    presenter.saveFileContent(f.copy(content = newContent))
                }
                is File.OcrResultFile -> {
                    // guardamos el texto OCR asociado a la imagen (busca por imageId)
                    presenter.saveOcrTextForImage(
                        imageId = f.originalImage.imageId,
                        newContent = newContent,
                        organizationId = organizationId,
                        groupId = groupId
                    )
                }
                else -> {
                    showError("Este tipo de archivo no es editable")
                    binding.saveButton.isEnabled = true
                }
            }
        }

        // 7) Volver
        binding.backButton.setOnClickListener { finish() }
    }

    // Hook de BaseActivity
    override fun onUserAuthenticated(user: FirebaseUser) {
        if (user.isAnonymous) {
            Toast.makeText(this, "Usuario anónimo, volviendo al login.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupUIForFile(file: File) {
        when (file) {
            is File.TextFile -> {
                // Solo texto editable
                binding.imageViewPreview.visibility = View.GONE
                binding.fileContentEditText.visibility = View.VISIBLE
                binding.fileContentEditText.isEnabled = true
                binding.fileContentEditText.setText(file.content)
                binding.saveButton.visibility = View.VISIBLE
            }
            is File.ImageFile -> {
                // Imagen; si hay texto vinculado, lo mostramos solo lectura
                binding.imageViewPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(file.storageInfo.downloadUrl)
                    .into(binding.imageViewPreview)

                if (!file.linkedOcrTextId.isNullOrEmpty()) {
                    loadOcrText(file.linkedOcrTextId)
                } else {
                    binding.fileContentEditText.visibility = View.GONE
                }
                binding.saveButton.visibility = View.GONE
            }
            is File.OcrResultFile -> {
                // Imagen + texto editable en un solo detalle
                binding.imageViewPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(file.originalImage.downloadUrl)
                    .into(binding.imageViewPreview)

                binding.fileContentEditText.visibility = View.VISIBLE
                binding.fileContentEditText.isEnabled = true
                binding.fileContentEditText.setText(file.extractedText)
                binding.saveButton.visibility = View.VISIBLE
            }
            else -> {
                showError("Tipo de archivo no soportado para vista previa")
                binding.fileContentEditText.visibility = View.GONE
                binding.saveButton.visibility = View.GONE
            }
        }
    }

    // Carga de texto OCR vinculado a una imagen (solo lectura en la vista de imagen pura)
    private fun loadOcrText(ocrTextId: String) {
        FirebaseDatabase.getInstance()
            .getReference("files")
            .child(ocrTextId)
            .get()
            .addOnSuccessListener { snapshot ->
                val textFile = snapshot.getValue(File.TextFile::class.java)
                textFile?.let {
                    binding.fileContentEditText.visibility = View.VISIBLE
                    binding.fileContentEditText.isEnabled = false
                    binding.fileContentEditText.setText(it.content)
                }
            }
    }

    // ==== FileDetailContract.View ====

    override fun showFileContent(file: File.TextFile) {
        // Cuando el presenter trae contenido desde RTDB
        binding.fileContentEditText.visibility = View.VISIBLE
        binding.fileContentEditText.isEnabled = true
        binding.fileContentEditText.setText(file.content)
        currentFile = file
    }

    override fun showLoading(isLoading: Boolean) {
        // Si no tienes progress bar, al menos bloquea el botón
        binding.saveButton.isEnabled = !isLoading
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.saveButton.isEnabled = true
    }

    override fun showSaveSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Actualiza el estado local por si no cerráramos aún
        val newText = binding.fileContentEditText.text.toString()
        currentFile = when (val f = currentFile) {
            is File.OcrResultFile -> f.copy(extractedText = newText)
            is File.TextFile      -> f.copy(content = newText)
            else                  -> currentFile
        }

        // Devuelve OK y cierra: GroupDetailActivity recargará la lista
        setResult(RESULT_OK)
        finish()
    }
}
