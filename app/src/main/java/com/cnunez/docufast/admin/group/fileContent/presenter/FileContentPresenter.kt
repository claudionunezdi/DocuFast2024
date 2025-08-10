package com.cnunez.docufast.admin.group.fileContent.presenter

import com.cnunez.docufast.admin.group.fileContent.contract.FileContentContract
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.TextFile
import com.cnunez.docufast.common.dataclass.File.OcrResultFile
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileContentPresenter(
    private val view: FileContentContract.View,
    private val model: FileContentContract.Model,
    private val userDao: UserDaoRealtime
) : FileContentContract.Presenter {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentFile: File? = null

    override fun loadFileContent(fileId: String) {
        scope.launch {
            view.showLoading()
            try {
                val file = model.getFileById(fileId)
                    ?: throw Exception("Archivo no encontrado")

                currentFile = file
                when (file) {
                    is TextFile, is OcrResultFile -> view.showContent(file)
                    else -> view.showError("Tipo de archivo no soportado")
                }
            } catch (e: Exception) {
                view.showError(e.message.orEmpty())
            } finally {
                view.hideLoading()
            }
        }
    }

    override fun saveFileContent(fileId: String, newContent: String) {
        val file = currentFile ?: run {
            view.showError("Archivo no cargado")
            return
        }

        scope.launch {
            view.showLoading()
            try {
                when (file) {
                    is TextFile -> {
                        val updated = file.copy(content = newContent)
                        if (model.updateFile(updated)) {
                            currentFile = updated
                            view.showContent(updated)
                            view.showSuccess("Cambios guardados")
                        } else {
                            view.showError("Error al guardar")
                        }
                    }
                    is OcrResultFile -> {
                        val updated = file.copy(extractedText = newContent)
                        if (model.updateFile(updated)) {
                            currentFile = updated
                            view.showContent(updated)
                            view.showSuccess("Texto OCR actualizado")
                        } else {
                            view.showError("Error al actualizar OCR")
                        }
                    }
                    else -> view.showError("Tipo de archivo no editable")
                }
            } catch (e: Exception) {
                view.showError(e.message.orEmpty())
            } finally {
                view.hideLoading()
            }
        }
    }

    override fun deleteFile(fileId: String) {
        scope.launch {
            view.showLoading()
            try {
                if (model.deleteFile(fileId)) {
                    view.onFileDeleted()
                } else {
                    view.showError("Error al eliminar el archivo")
                }
            } catch (e: Exception) {
                view.showError(e.message ?: "Error al eliminar")
            } finally {
                view.hideLoading()
            }
        }
    }

    override fun getUserName(userId: String, callback: (String?) -> Unit) {
        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.getById(userId)
                }
                callback(user?.name)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
    override fun onDestroy() {
        scope.cancel()
    }
}