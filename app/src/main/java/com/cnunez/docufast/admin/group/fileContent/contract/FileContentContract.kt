package com.cnunez.docufast.admin.group.fileContent.contract

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.ImageFile
import com.cnunez.docufast.common.dataclass.File.TextFile

interface FileContentContract {
    interface View {
        fun showContent(file: File)
        fun showError(message: String)
        fun onFileDeleted()

        fun showLoading()
        fun hideLoading()
        fun showSuccess(message: String)


    }

    interface Presenter {
        fun loadFileContent(fileId: String)
        fun saveFileContent(fileId: String, newContent: String)
        fun deleteFile(fileId: String)
        fun onDestroy()
        fun getUserName(userId: String, callback: (String?) -> Unit)
    }

    interface Model {
        suspend fun getFileById(id: String): File?
        suspend fun updateFile(file: File): Boolean
        suspend fun deleteFile(fileId: String): Boolean

    }
}