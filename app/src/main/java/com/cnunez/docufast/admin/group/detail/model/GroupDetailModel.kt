package com.cnunez.docufast.admin.group.detail.model

import com.cnunez.docufast.common.dataclass.WorkGroup
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailModel {

    interface OnGroupDetailListener {
        fun onSuccess(group: WorkGroup)
        fun onError(message: String)
    }

    fun getGroupDetails(groupId: String, listener: OnGroupDetailListener) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val group = document.toObject(WorkGroup::class.java)
                    if (group != null) {
                        listener.onSuccess(group)
                    } else {
                        listener.onError("Group not found")
                    }
                } else {
                    listener.onError("Group not found")
                }
            }
            .addOnFailureListener { exception ->
                listener.onError("Error getting group details: ${exception.message}")
            }
    }
}