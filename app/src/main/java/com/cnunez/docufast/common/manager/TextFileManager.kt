package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.TextFile
import com.google.firebase.database.FirebaseDatabase

class TextFileManager(private val database: FirebaseDatabase = FirebaseDatabase.getInstance()) {
    private val dao = TextFileDaoFirebase(database)

    suspend fun save(textFile: TextFile): String = dao.insert(textFile)
    suspend fun update(textFile: TextFile) = dao.update(textFile)
    suspend fun getAll(): List<TextFile> = dao.getAll()
    suspend fun getById(id: String): TextFile? = dao.getTextFileById(id)
}