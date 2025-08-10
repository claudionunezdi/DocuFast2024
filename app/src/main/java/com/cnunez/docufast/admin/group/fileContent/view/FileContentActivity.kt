package com.cnunez.docufast.admin.group.fileContent.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.cnunez.docufast.R
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.base.SessionManager.getCurrentUser
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.ImageFile
import com.cnunez.docufast.common.dataclass.File.OcrResultFile
import com.cnunez.docufast.common.dataclass.File.TextFile
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.cnunez.docufast.admin.group.fileContent.contract.FileContentContract
import com.cnunez.docufast.admin.group.fileContent.model.FileContentModel
import com.cnunez.docufast.admin.group.fileContent.presenter.FileContentPresenter
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class FileContentActivity : BaseActivity(), FileContentContract.View {

    private lateinit var presenter: FileContentContract.Presenter
    private lateinit var contentEditText: EditText
    private lateinit var uploaderTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var fabEdit: ExtendedFloatingActionButton
    private lateinit var fabDelete: FloatingActionButton
    private lateinit var fabSave: ExtendedFloatingActionButton
    private lateinit var progressBar: ProgressBar

    private var currentFile: File? = null
    private var isAdmin = false
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_file_detail)

        initViews()
        initPresenter()
        checkUserPermissions()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        contentEditText = findViewById(R.id.editTextContent)
        uploaderTextView = findViewById(R.id.textViewUploader)
        dateTextView = findViewById(R.id.textViewUploadDate)
        imageView = findViewById(R.id.imageViewDocument)
        fabEdit = findViewById(R.id.fabEdit)
        fabSave = findViewById(R.id.fabSave)
        fabDelete = findViewById(R.id.fabDelete)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fabEdit.setOnClickListener { toggleEditMode(true) }
        fabSave.setOnClickListener { saveChanges() }
        fabDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun initPresenter() {
        val fileId = intent.getStringExtra("FILE_ID").orEmpty()
        val db = FirebaseDatabase.getInstance()
        val storageManager = FileStorageManager.getInstance()
        val fileDao = FileDaoRealtime(db, storageManager)
        val userDao = UserDaoRealtime(db)  // Crea instancia de UserDao

        val model = FileContentModel(fileDao, userDao)  // Pasa ambos DAOs al modelo
        presenter = FileContentPresenter(this, model, userDao)  // Pasa userDao al presentador

        presenter.loadFileContent(fileId)
    }

    private fun checkUserPermissions() {
        isAdmin = getCurrentUser()?.email?.endsWith("@admin.com") == true
        fabEdit.visibility = if (isAdmin) View.VISIBLE else View.GONE
        fabDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    override fun showLoading() {
        runOnUiThread { progressBar.visibility = View.VISIBLE }
    }

    override fun hideLoading() {
        runOnUiThread { progressBar.visibility = View.GONE }
    }

    override fun showContent(file: File) {
        currentFile = file
        title = file.name

        showMetadata(file)
        showFileContent(file)
    }

    private fun showMetadata(file: File) {
        runOnUiThread {
            uploaderTextView.text = buildString {
                append("Subido por: ${file.metadata.creatorName}\n")  // Usa el nombre ya almacenado
                append("Grupo: ${file.metadata.groupId}\n")
                append("Organización: ${file.metadata.organizationId}")
            }
        }
    }

    private fun showFileContent(file: File) {
        contentEditText.visibility = View.VISIBLE

        when (file) {
            is TextFile -> {
                imageView.visibility = View.GONE
                contentEditText.setText(file.content)
            }
            is ImageFile -> {
                loadImage(file.storageInfo.downloadUrl)
                contentEditText.setText("") // Espacio para notas
            }
            is OcrResultFile -> {
                loadImage(file.originalImage.downloadUrl)
                contentEditText.setText(file.extractedText)
            }
        }
    }

    private fun loadImage(url: String) {
        imageView.visibility = View.VISIBLE
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_broken_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString) ?: return dateString
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun toggleEditMode(enable: Boolean) {
        isEditMode = enable && isAdmin
        contentEditText.isEnabled = isEditMode

        fabEdit.visibility = if (isEditMode) View.GONE else View.VISIBLE
        fabSave.visibility = if (isEditMode) View.VISIBLE else View.GONE

        if (isEditMode) {
            contentEditText.requestFocus()
            showToast("Modo edición activado")
        }
    }

    private fun saveChanges() {
        val newContent = contentEditText.text.toString()
        when (val file = currentFile) {
            is TextFile -> presenter.saveFileContent(file.id, newContent)
            is ImageFile -> showToast("No se pueden guardar notas en imágenes directamente")
            is OcrResultFile -> presenter.saveFileContent(file.id, newContent)
            else -> showToast("Tipo de archivo no editable")
        }
        toggleEditMode(false)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar archivo")
            .setMessage("¿Estás seguro de que deseas eliminar este archivo permanentemente?")
            .setPositiveButton("Eliminar") { _, _ -> currentFile?.id?.let(presenter::deleteFile) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onFileDeleted() {
        showToast("Archivo eliminado correctamente")
        finish()
    }

    override fun showError(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }

    override fun showSuccess(message: String) {
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isEditMode) showDiscardChangesDialog() else super.onBackPressed()
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Descartar cambios")
            .setMessage("¿Estás seguro de que quieres descartar los cambios?")
            .setPositiveButton("Descartar") { _, _ ->
                currentFile?.let(::showContent)
                toggleEditMode(false)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    companion object {
        fun start(context: Context, fileId: String) {
            context.startActivity(Intent(context, FileContentActivity::class.java).apply {
                putExtra("FILE_ID", fileId)
            })
        }
    }
}