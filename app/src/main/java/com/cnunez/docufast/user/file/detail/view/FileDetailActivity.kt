package com.cnunez.docufast.user.file.detail.view

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.TextFile
import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.cnunez.docufast.user.file.detail.presenter.FileDetailPresenter
import com.google.firebase.auth.FirebaseUser

class FileDetailActivity : BaseActivity(), FileDetailContract.View {
    private lateinit var presenter: FileDetailContract.Presenter
    private lateinit var fileContentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var imageViewPreview: ImageView

    private var currentFile: TextFile? = null
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
            val updatedFile = currentFile?.copy(content = newContent)
            if (updatedFile != null) {
                presenter.saveFileContent(updatedFile)
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        // Handle user authentication if needed
        if (user.isAnonymous) {
            Toast.makeText(this, "Anonymous user detected. Redirecting to login.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Welcome  ${user.email}", Toast.LENGTH_SHORT).show()
            // Handle authenticated user
        }
    }

    override fun showFileContent(file: File.TextFile) {
        fileContentEditText.setText(file.content)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}