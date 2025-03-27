package com.cnunez.docufast.user.group.detail.model

import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailModel : GroupDetailContract.Model {
    private val db = FirebaseFirestore.getInstance()

    override fun fetchGroupFiles(groupId: String, organizationId: String, callback: (List<TextFile>?, String?) -> Unit) {
        db.collection("organizations").document(organizationId)
            .collection("groups").document(groupId)
            .collection("files")
            .get()
            .addOnSuccessListener { documents ->
                val files = documents.map { it.toObject(TextFile::class.java) }
                callback(files, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception.message)
            }
    }
}