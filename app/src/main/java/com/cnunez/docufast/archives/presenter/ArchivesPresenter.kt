package com.cnunez.docufast.archives.presenter

import com.cnunez.docufast.archives.contract.ArchivesContract
import com.cnunez.docufast.archives.model.impl.ArchivesModelImpl
import java.io.File

class ArchivesPresenter(private val model: ArchivesModelImpl, private val view: ArchivesContract.View) : ArchivesContract.Presenter {
    override fun listFiles() {
        val files = model.listArchives()
        view.showFiles(files)
    }

    override fun openFile(file: File) {
        model.openFile(file)
    }

    override fun editFile(file: File) {
        model.editFile(file)
    }

    override fun viewFileContent(file: File) {
        view.viewFileContent(file)
    }
}