package com.cnunez.docufast.fileContent.model

import com.cnunez.docufast.fileContent.contract.FileContentContract
import com.cnunez.docufast.common.dataclass.TextFile
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FileContentModel(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : FileContentContract.Model {
    private val ref = database.getReference("textFiles")

    override suspend fun getTextFileById(id: String): TextFile? = withContext(Dispatchers.IO) {
        val snap = ref.child(id).get().await()
        if (!snap.exists()) return@withContext null
        TextFile.fromSnapshot(snap)
    }

    override suspend fun updateTextFile(textFile: TextFile): Unit = withContext(Dispatchers.IO) {
        ref.child(textFile.id).updateChildren(textFile.toMap()).await()
    }
}
