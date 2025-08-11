package com.cnunez.docufast.user.file.detail.contract

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.TextFile

interface FileDetailContract {
    interface View {
        fun showFileContent(file: TextFile)
        fun showError(message: String)
        fun showSaveSuccess(message: String = "Cambios guardados")
        fun showLoading(isLoading: Boolean) { /* default no-op */ }
    }

    interface Presenter {
        fun loadFileContent(fileId: String, organizationId: String)
        fun saveFileContent(file: File.TextFile)
        fun saveOcrTextForImage(
            imageId: String,
            newContent: String,
            organizationId: String,
            groupId: String? = null
        )



    }

    interface Model {
        fun fetchFileContent(fileId: String, organizationId: String, callback: (TextFile?, String?) -> Unit) // Add organizationId parameter
        fun updateFileContent(file: TextFile, newContent: String, callback: (Boolean, String?) -> Unit)
    }
}