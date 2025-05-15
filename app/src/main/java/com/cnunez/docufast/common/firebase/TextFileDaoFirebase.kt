package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.camera.model.TextFile
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class TextFileDaoFirebase(private val firebaseDatabase: FirebaseDatabase) {
    private val databaseReference = firebaseDatabase.getReference("text_files")

    suspend fun insert(textFile: TextFile): String {
        val key = databaseReference.push().key ?: throw Exception("Error generating key")
        databaseReference.child(key).setValue(textFile).await()
        return key
    }

    suspend fun getTextFileById(id: String): TextFile? {
        val snapshot = databaseReference.child(id).get().await()
        return snapshot.getValue(TextFile::class.java)
    }

    suspend fun getAllTextFiles(): List<TextFile> {
        val snapshot = databaseReference.get().await()
        return snapshot.children.mapNotNull { it.getValue(TextFile::class.java) }
    }

    suspend fun update(textFile: TextFile) {
        textFile.id?.let {
            databaseReference.child(it.toString()).setValue(textFile).await() // Conversión a String
        } ?: throw Exception("TextFile ID is null")
    }
}