package com.cnunez.docufast.archives.Contract

import java.io.File


interface ArchivesContract {

    interface View {
        fun showFiles(files: List<File>)
        fun openFile(file: File)
        fun editFile(file: File)
    }

    interface Presenter {
        fun listFiles()
        fun openFile(file: File)
        fun editFile(file: File)
    }

    interface Model {
        fun listArchives(): List<File>
        fun openFile(file: File)
        fun editFile(file: File)
    }
}