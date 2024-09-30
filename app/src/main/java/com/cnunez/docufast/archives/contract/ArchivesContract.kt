package com.cnunez.docufast.archives.contract

import java.io.File

interface ArchivesContract {

    interface View {
        fun showFiles(files: List<File>)
        fun openFile(file: File)
        fun editFile(file: File)
        fun viewFileContent(file: File)
    }

    interface Presenter {
        fun listFiles()
        fun openFile(file: File)
        fun editFile(file: File)
        fun viewFileContent(file: File)
    }

    interface Model {
        fun listArchives(): List<File>
        fun openFile(file: File)
        fun editFile(file: File)
    }
}