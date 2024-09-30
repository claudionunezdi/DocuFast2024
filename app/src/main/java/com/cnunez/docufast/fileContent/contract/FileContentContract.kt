package com.cnunez.docufast.fileContent.contract

import java.io.File

interface FileContentContract {

    interface View {
        fun showFileContent(content: String)
        fun showError(message:String)
    }

    interface Presenter{
        fun loadFileContent(file:File)
        fun saveFileContent(file:File, content:String)
    }

    interface Model{
        fun readFile(file:File):String
        fun writeFile(file:File, content:String)
    }
}