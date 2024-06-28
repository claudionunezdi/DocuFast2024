package com.cnunez.docufast.fileContent.Presenter

import com.cnunez.docufast.fileContent.Contract.FileContentContract
import java.io.File

class FileContentPresenter(
    private val view: FileContentContract.View,
    private val model: FileContentContract.Model
) : FileContentContract.Presenter {
    override fun loadFileContent(file: File) {
        try {
            val content = model.readFile(file)
            view.showFileContent(content)
        } catch (e: Exception) {
            view.showError(e.message ?: "Error loading file content")
        }
    }

    override fun saveFileContent(file: File, content: String) {
        try {
            model.writeFile(file, content)
            view.showFileContent(content)
        } catch (e: Exception) {
            view.showError(e.message ?: "Error saving file content")
        }
    }
}