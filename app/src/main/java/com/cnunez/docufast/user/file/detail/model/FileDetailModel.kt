package com.cnunez.docufast.user.file.detail.model

import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FileDetailModel : FileDetailContract.Model {
    private val database = FirebaseDatabase.getInstance()

    override fun fetchFileContent(
        fileId: String,
        organizationId: String,
        callback: (TextFile?, String?) -> Unit
    ) {
        val fileRef = database.getReference("organizations/$organizationId/textFiles/$fileId")

        fileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val file = snapshot.getValue(TextFile::class.java)?.copy(id = fileId, organizationId = organizationId)
                callback(file, null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, error.message)
            }
        })
    }

    override fun updateFileContent(
        file: TextFile,
        newContent: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val fileRef = database.getReference("organizations/${file.organizationId}/textFiles/${file.id}")
        val updatedFile = file.copy(content = newContent)

        fileRef.setValue(updatedFile)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.message) }
    }
}
