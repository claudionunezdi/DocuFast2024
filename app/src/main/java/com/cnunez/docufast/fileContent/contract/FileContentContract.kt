package com.cnunez.docufast.fileContent.contract

import com.cnunez.docufast.common.dataclass.TextFile

interface FileContentContract {
    interface View {
        fun showContent(textFile: TextFile)
        fun showError(message: String)
    }

    interface Presenter {
        fun loadFileContent(fileId: String)
        fun saveFileContent(fileId: String, newContent: String)
    }

    interface Model {
        suspend fun getTextFileById(id: String): TextFile?
        suspend fun updateTextFile(textFile: TextFile)
    }
}
