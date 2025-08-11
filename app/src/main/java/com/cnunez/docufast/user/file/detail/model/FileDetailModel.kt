package com.cnunez.docufast.user.file.detail.model

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.user.file.detail.contract.FileDetailContract
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.cnunez.docufast.common.dataclass.File.TextFile
import com.cnunez.docufast.common.dataclass.FileType

class FileDetailModel : FileDetailContract.Model {
    private val database = FirebaseDatabase.getInstance()
    private val db = FirebaseDatabase.getInstance().reference.child("files")

    override fun fetchFileContent(
        fileId: String,
        organizationId: String, // no lo usamos aquí, lo mantiene el contrato
        callback: (File.TextFile?, String?) -> Unit
    ) {
        db.child(fileId).get()
            .addOnSuccessListener { snap ->
                val type = snap.child("type").getValue(String::class.java)
                if (type == FileType.TEXT.name) {
                    val tf = snap.getValue(File.TextFile::class.java)?.copy(
                        id = fileId
                    )
                    callback(tf, null)
                } else {
                    callback(null, "El archivo no es de tipo TEXT")
                }
            }
            .addOnFailureListener { e -> callback(null, e.message) }
    }

    override fun updateFileContent(
        file: File.TextFile,
        newContent: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val updates = mapOf<String, Any?>(
            "content" to newContent
        )
        db.child(file.id).updateChildren(updates)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }
}
