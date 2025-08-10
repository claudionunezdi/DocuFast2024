package com.cnunez.docufast.user.group.detail.presenter

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class GroupDetailPresenter(
    private val view: GroupDetailContract.View,
    private val model: GroupDetailContract.Model
) : GroupDetailContract.Presenter, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun loadGroupFiles(groupId: String, organizationId: String, filterType: FileType?) {
        launch(Dispatchers.IO) {
            try {
                model.fetchGroupFiles(groupId, organizationId, filterType) { files, error ->
                    launch(Dispatchers.Main) {
                        when {
                            error != null -> view.showError(error)
                            !files.isNullOrEmpty() -> view.showFiles(files, filterType)
                            else -> view.showError("No se encontraron archivos en el grupo")
                        }
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    view.showError("Error al cargar archivos: ${e.message}")
                }
            }
        }
    }

    override fun onFileSelected(file: File) {
        launch(Dispatchers.IO) {
            try {
                model.getFileDownloadUrl(file) { downloadUrl, error ->
                    launch(Dispatchers.Main) {
                        when {
                            error != null -> view.showError(error)
                            downloadUrl != null -> {
                                val updatedFile = when (file) {
                                    is File.TextFile -> file.copy(
                                        storageInfo = file.storageInfo.copy(downloadUrl = downloadUrl)
                                    )
                                    is File.ImageFile -> file.copy(
                                        storageInfo = file.storageInfo.copy(downloadUrl = downloadUrl)
                                    )
                                    else -> file
                                }
                                view.showFileDetail(updatedFile)
                            }
                            else -> view.showFileDetail(file)
                        }
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    view.showError("Error al procesar archivo: ${e.message}")
                }
            }
        }
    }

    override fun observeGroupFiles(groupId: String, organizationId: String, filterType: FileType?) {
        launch(Dispatchers.IO) {
            model.observeGroupFiles(groupId, organizationId, filterType) { files, error ->
                launch(Dispatchers.Main) {
                    when {
                        error != null -> view.showError(error)
                        !files.isNullOrEmpty() -> view.showFiles(files, filterType)
                        else -> view.showError("No hay archivos aún")
                    }
                }
            }
        }
    }

    fun onDestroy() {
        job.cancel()
    }
}