package com.cnunez.docufast.admin.group.create.presenter

import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class CreateGroupPresenter(private val view: CreateGroupContract.View) :
    CreateGroupContract.Presenter {

    override fun createGroup(
        name: String,
        description: String,
        members: List<User>,
        files: List<File>
    ) {
        val group = WorkGroup(
            id = 0,
            name = name,
            description = description,
            members = members,
            files = files
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .add(group)
            .addOnSuccessListener {
                view.onGroupCreated(group)
            }
            .addOnFailureListener { exception ->
                view.onError("Error creating group: ${exception.message}")
            }
    }
}