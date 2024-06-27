package com.cnunez.docufast.archives.Presenter

import com.cnunez.docufast.archives.Contract.ArchivesContract
import com.cnunez.docufast.archives.Model.impl.ArchivesModelImpl
import java.io.File

class ArchivesPresenter(private val model: ArchivesModelImpl, private val view: ArchivesContract.View) : ArchivesContract.Presenter {
    override fun listFiles() {
        val files = model.listArchives()
        view.showFiles(files)
    }

    override fun openFile(file: File) {
        view.openFile(file)
    }
}