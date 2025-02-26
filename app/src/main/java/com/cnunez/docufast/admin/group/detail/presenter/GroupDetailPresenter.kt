package com.cnunez.docufast.admin.group.detail.view

import com.cnunez.docufast.common.dataclass.WorkGroup
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailPresenter(private val view: GroupDetailContract.View) : GroupDetailContract.Presenter {

    override fun loadGroupDetails(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val group = document.toObject(WorkGroup::class.java)
                    if (group != null) {
                        view.showGroupDetails(group)
                    } else {
                        view.showError("Group not found")
                    }
                } else {
                    view.showError("Group not found")
                }
            }
            .addOnFailureListener { exception ->
                view.showError("Error getting group details: ${exception.message}")
            }
    }
}