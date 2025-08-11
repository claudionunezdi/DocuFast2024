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
        android.util.Log.d("PRES/Select", "file=${file.id} type=${file.type} path='${file.storageInfo.path}' url='${file.storageInfo.downloadUrl}'")

        // 1) TextFile no necesita Storage → abrir directo
        if (file is File.TextFile) {
            view.showFileDetail(file)
            return
        }

        // 2) Para Image/OCR: ¿realmente necesitamos pedir downloadUrl?
        val needsUrl = when (file) {
            is File.ImageFile, is File.OcrResultFile ->
                file.storageInfo.downloadUrl.isBlank() && file.storageInfo.path.isNotBlank()
            else -> false
        }

        if (!needsUrl) {
            // Ya tenemos URL o no hay path → abrir directo
            view.showFileDetail(file)
            return
        }

        // 3) Obtener downloadUrl solo si hace falta
        launch(Dispatchers.IO) {
            try {
                model.getFileDownloadUrl(file) { downloadUrl, error ->
                    launch(Dispatchers.Main) {
                        when {
                            error != null -> view.showError(error)
                            !downloadUrl.isNullOrBlank() -> {
                                val updated = when (file) {
                                    is File.ImageFile -> file.copy(
                                        storageInfo = file.storageInfo.copy(downloadUrl = downloadUrl)
                                    )
                                    is File.OcrResultFile -> file.copy(
                                        storageInfo = file.storageInfo.copy(downloadUrl = downloadUrl),
                                        originalImage = file.originalImage.copy(downloadUrl = downloadUrl)
                                    )
                                    else -> file
                                }
                                view.showFileDetail(updated)
                            }
                            else -> view.showFileDetail(file)
                        }
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    view.showError("Error al obtener URL: ${e.message}")
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