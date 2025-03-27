package com.cnunez.docufast.user.file.detail.view

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.cnunez.docufast.user.file.detail.presenter.FileDetailPresenter

class FileDetailActivity : AppCompatActivity(), FileDetailContract.View {
    private lateinit var presenter: FileDetailContract.Presenter
    private lateinit var fileContentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var imageViewPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_content)

        val fileId = intent.getStringExtra("fileId") ?: return
        val organizationId = intent.getStringExtra("organizationId") ?: return
        val imageUri = intent.getParcelableExtra<Uri>("imageUri")

        fileContentEditText = findViewById(R.id.fileContentEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        imageViewPreview = findViewById(R.id.imageViewPreview)

        presenter = FileDetailPresenter(this)
        presenter.loadFileContent(fileId, organizationId)

        imageUri?.let {
            imageViewPreview.setImageURI(it)
        }

        saveButton.setOnClickListener {
            val newContent = fileContentEditText.text.toString()
            presenter.saveFileContent(TextFile(fileId, newContent, organizationId = organizationId), newContent)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun showFileContent(file: TextFile) {
        fileContentEditText.setText(file.content)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}