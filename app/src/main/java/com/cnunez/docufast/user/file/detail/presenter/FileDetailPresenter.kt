package com.cnunez.docufast.user.file.detail.presenter

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.cnunez.docufast.user.file.detail.model.FileDetailModel
import com.cnunez.docufast.common.dataclass.File.TextFile
import com.cnunez.docufast.common.firebase.AppDatabase.fileDao
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileDetailPresenter(
    private val view: FileDetailContract.View,
    private val model: FileDetailContract.Model = FileDetailModel(),
    private val fileDao: FileDaoRealtime = FileDaoRealtime(
        com.google.firebase.database.FirebaseDatabase.getInstance(),
        com.cnunez.docufast.common.firebase.storage.FileStorageManager.getInstance())
) : FileDetailContract.Presenter {

    override fun loadFileContent(fileId: String, organizationId: String) {
        android.util.Log.d("FD/Presenter","loadFileContent fileId=$fileId")
        model.fetchFileContent(fileId, organizationId) { file, error ->
            if (error != null) {
                view.showError(error)
            } else if (file != null) {
                view.showFileContent(file)
            }
        }
    }

    override fun saveFileContent(file: File.TextFile) {
        val orgId = file.metadata.organizationId
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                fileDao.updateTextFileContentAndStorage(file.id, file.content, orgId)
            }
            res.fold(
                onSuccess = { view.showSaveSuccess("Cambios guardados") },
                onFailure = { view.showError(it.message ?: "Error al guardar") }
            )
        }
    }

    override fun saveOcrTextForImage(
        imageId: String,
        newContent: String,
        organizationId: String,
        groupId: String?
    ) {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                fileDao.saveOcrTextSmart(organizationId, imageId, newContent, groupId, alsoUploadToStorage = true)
            }
            res.fold(
                onSuccess = { view.showSaveSuccess("Cambios guardados") },
                onFailure = { view.showError(it.message ?: "Error al guardar") }
            )
        }
    }


}