package com.cnunez.docufast.user.file.detail.presenter

import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.cnunez.docufast.user.file.detail.model.FileDetailModel
import com.cnunez.docufast.common.dataclass.File.TextFile

class FileDetailPresenter(
    private val view: FileDetailContract.View,
    private val model: FileDetailContract.Model = FileDetailModel()
) : FileDetailContract.Presenter {

    override fun loadFileContent(fileId: String, organizationId: String) {
        model.fetchFileContent(fileId, organizationId) { file, error ->
            if (error != null) {
                view.showError(error)
            } else if (file != null) {
                view.showFileContent(file)
            }
        }
    }

    override fun saveFileContent(file: TextFile, newContent: String) {
        model.updateFileContent(file, newContent) { success, error ->
            if (success) {
                view.showFileContent(file.copy(content = newContent))
            } else {
                view.showError(error ?: "Unknown error")
            }
        }
    }

    override fun saveFileContent(file: TextFile) {
        TODO("Not yet implemented")
    }
}