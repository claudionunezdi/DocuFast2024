package com.cnunez.docufast.fileContent.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R

import com.cnunez.docufast.fileContent.Contract.FileContentContract
import com.cnunez.docufast.fileContent.Model.fileContentModel
import com.cnunez.docufast.fileContent.Presenter.FileContentPresenter
import java.io.File



class FileContentActivity : AppCompatActivity(), FileContentContract.View {
    private lateinit var presenter: FileContentContract.Presenter
    private lateinit var fileContentEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_content)

        val filePath = intent.getStringExtra("filePath")

        fileContentEditText = findViewById(R.id.fileContentEditText)
        saveButton = findViewById(R.id.saveButton)

        presenter = FileContentPresenter(this, fileContentModel())
        presenter.loadFileContent(File(filePath))

        saveButton.setOnClickListener {
            val newContent = fileContentEditText.text.toString()
            presenter.saveFileContent(File(filePath), newContent)
        }
    }

    override fun showFileContent(content: String) {
        fileContentEditText.setText(content)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}