package com.cnunez.docufast.fileContent.model

import java.io.File

interface FileContentModel{
    fun readFile(file: File): String

    fun writeFile(file: File, content: String)
}