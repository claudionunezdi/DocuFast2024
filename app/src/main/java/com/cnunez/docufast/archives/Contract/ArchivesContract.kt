package com.cnunez.docufast.archives.Contract

import java.io.File


interface ArchivesContract {

    interface View {
        fun showFiles(files: List<File>)
        fun openFile(file: File)
    }

    interface Presenter {
        fun listFiles()
        fun openFile(file: File)
    }

    interface Model {
    }
}