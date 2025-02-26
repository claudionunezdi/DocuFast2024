package com.cnunez.docufast.admin.group.edit.model


import com.cnunez.docufast.common.dataclass.WorkGroup
import com.cnunez.docufast.admin.group.edit.contract.ListContract
import com.google.firebase.firestore.FirebaseFirestore

class ListModel : ListContract.Model {

    private val db= FirebaseFirestore.getInstance()


    override fun fetchGroups(callback: (List<WorkGroup>?, String?) -> Unit) {
        db.collection("groups").get()
            .addOnSuccessListener{result->
                val groups = result.map{document->document.toObject(WorkGroup::class.java)}
                callback(groups,null)
            }
            .addOnFailureListener{exception ->
                callback(emptyList(),exception.message)
            }
    }
}