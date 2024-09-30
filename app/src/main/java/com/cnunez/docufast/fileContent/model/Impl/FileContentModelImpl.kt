package com.cnunez.docufast.fileContent.model.Impl

import com.cnunez.docufast.fileContent.contract.FileContentContract
import com.cnunez.docufast.fileContent.view.FileContentActivity
import java.io.File

class FileContentModelImpl(fileContentActivity: FileContentActivity) : FileContentContract.Model{
    override fun readFile(file: File): String {
        return file.readText()
    }

    override fun writeFile(file: File, content: String) {
        file.writeText(content)
    }
}