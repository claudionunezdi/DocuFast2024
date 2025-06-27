package com.cnunez.docufast.fileContent.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.cnunez.docufast.R
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.fileContent.contract.FileContentContract
import com.cnunez.docufast.fileContent.model.FileContentModel
import com.cnunez.docufast.fileContent.presenter.FileContentPresenter
import com.google.firebase.auth.FirebaseUser

class FileContentActivity : BaseActivity(), FileContentContract.View {
    private lateinit var presenter: FileContentContract.Presenter
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var fileId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_content)

        contentEditText = findViewById(R.id.fileContentEditText)
        saveButton      = findViewById(R.id.saveButton)
        fileId          = intent.getStringExtra("FILE_ID").orEmpty()

        presenter = FileContentPresenter(this, FileContentModel())
        presenter.loadFileContent(fileId)

        saveButton.setOnClickListener {
            presenter.saveFileContent(fileId, contentEditText.text.toString())
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {}

    override fun showContent(textFile: TextFile) {
        contentEditText.setText(textFile.content)
        Toast.makeText(this, "Archivo: ${textFile.fileName}", Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}