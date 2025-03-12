package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.File
import com.google.firebase.firestore.FirebaseFirestore

class ArchivesManager {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun fetchFiles(groupId: String, onComplete: (List<File>?, String?) -> Unit) {
        db.collection("groups").document(groupId).collection("files").get()
            .addOnSuccessListener { result ->
                val files = result.map { it.toObject(File::class.java) }
                onComplete(files, null)
            }
            .addOnFailureListener { exception ->
                onComplete(null, exception.message)
            }
    }
}