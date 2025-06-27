package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.TextFile
import com.google.firebase.database.FirebaseDatabase

class TextFileManager(database: FirebaseDatabase) {
    private val dao = TextFileDaoFirebase(database)

    suspend fun save(textFile: TextFile): String = dao.insert(textFile)

    suspend fun update(textFile: TextFile) = dao.update(textFile)

    suspend fun loadAll(): List<TextFile> = dao.getAll()

    suspend fun loadById(id: String): TextFile? = dao.getTextFileById(id)
}
