package com.cnunez.docufast.archives.Model

import java.io.File

interface ArchivesModel {
    fun listArchives(): List<File>
    fun openFile(file: File)

    fun editFile(file: File)
}