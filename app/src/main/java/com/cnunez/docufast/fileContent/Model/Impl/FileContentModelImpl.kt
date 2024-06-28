package com.cnunez.docufast.fileContent.Model.Impl

import com.cnunez.docufast.fileContent.Contract.FileContentContract
import java.io.File

class FileContentModelImpl : FileContentContract.Model{
    override fun readFile(file: File): String {
        return file.readText()
    }

    override fun writeFile(file: File, content: String) {
        file.writeText(content)
    }
}