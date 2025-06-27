package com.cnunez.docufast.fileContent.presenter

import com.cnunez.docufast.fileContent.contract.FileContentContract
import com.cnunez.docufast.common.dataclass.TextFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FileContentPresenter(
    private val view: FileContentContract.View,
    private val model: FileContentContract.Model
) : FileContentContract.Presenter {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentFile: TextFile? = null

    override fun loadFileContent(fileId: String) {
        scope.launch {
            try {
                val tf = model.getTextFileById(fileId)
                    ?: throw Exception("Archivo no encontrado")
                currentFile = tf
                view.showContent(tf)
            } catch (e: Exception) {
                view.showError(e.message.orEmpty())
            }
        }
    }

    override fun saveFileContent(fileId: String, newContent: String) {
        val tf = currentFile
        if (tf == null || tf.id != fileId) {
            view.showError("Operación inválida")
            return
        }
        scope.launch {
            try {
                val updated = tf.copy(content = newContent)
                model.updateTextFile(updated)
                view.showContent(updated)
            } catch (e: Exception) {
                view.showError(e.message.orEmpty())
            }
        }
    }
}
