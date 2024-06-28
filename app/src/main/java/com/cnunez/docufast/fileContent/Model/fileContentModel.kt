package com.cnunez.docufast.fileContent.Model

import java.io.File

interface fileContentModel{
    fun readFile(file: File): String

    fun writeFile(file: File, content: String)
}