package com.cnunez.docufast.admin.group.detail.model

import android.content.Context
import com.cnunez.docufast.admin.group.detail.view.GroupDetailContract
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailModel(private val context: Context) : GroupDetailContract.Model {

    override fun getGroupDetails(
        groupId: String,
        listener: GroupDetailContract.Model.OnGroupDetailListener
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val group = document.toObject(Group::class.java)
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