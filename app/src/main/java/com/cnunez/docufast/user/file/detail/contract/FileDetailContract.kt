package com.cnunez.docufast.user.file.detail.contract

import com.cnunez.docufast.common.dataclass.TextFile

interface FileDetailContract {
    interface View {
        fun showFileContent(file: TextFile)
        fun showError(message: String)
    }

    interface Presenter {
        fun loadFileContent(fileId: String, organizationId: String) // Add organizationId parameter
        fun saveFileContent(file: TextFile, newContent: String)
    }

    interface Model {
        fun fetchFileContent(fileId: String, organizationId: String, callback: (TextFile?, String?) -> Unit) // Add organizationId parameter
        fun updateFileContent(file: TextFile, newContent: String, callback: (Boolean, String?) -> Unit)
    }
}