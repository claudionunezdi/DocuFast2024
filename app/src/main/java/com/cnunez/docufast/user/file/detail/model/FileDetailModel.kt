package com.cnunez.docufast.user.file.detail.model

import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.cnunez.docufast.common.FirebaseUtils
import com.google.firebase.firestore.FirebaseFirestore

class FileDetailModel : FileDetailContract.Model {
    private val db = FirebaseFirestore.getInstance()

    override fun fetchFileContent(fileId: String, organizationId: String, callback: (TextFile?, String?) -> Unit) {
        db.collection("organizations").document(organizationId)
            .collection("textFiles").document(fileId)
            .get()
            .addOnSuccessListener { document ->
                val file = document.toObject(TextFile::class.java)
                callback(file, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception.message)
            }
    }

    override fun updateFileContent(file: TextFile, newContent: String, callback: (Boolean, String?) -> Unit) {
        FirebaseUtils.saveTextFileToFirebase(db, file.copy(content = newContent), {
            callback(true, null)
        }, { exception ->
            callback(false, exception.message)
        })
    }
}