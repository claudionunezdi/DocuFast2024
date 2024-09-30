package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.camera.model.TextFile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TextFileDaoFirebase {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("text_files")

    suspend fun insert(textFile: TextFile): String {
        val document = collection.add(textFile).await()
        return document.id
    }

    suspend fun getTextFileById(id: String): TextFile? {
        val document = collection.document(id).get().await()
        return document.toObject(TextFile::class.java)
    }

    suspend fun getAllTextFiles(): List<TextFile> {
        val snapshot = collection.get().await()
        return snapshot.toObjects(TextFile::class.java)
    }

    suspend fun getTextFileByUri(uri: String): TextFile? {
        val snapshot = collection.whereEqualTo("uri", uri).get().await()
        return if (snapshot.documents.isNotEmpty()) {
            snapshot.documents[0].toObject(TextFile::class.java)
        } else {
            null
        }
    }

    suspend fun update(textFile: TextFile) {
        collection.document(textFile.id.toString()).set(textFile).await()
    }
}