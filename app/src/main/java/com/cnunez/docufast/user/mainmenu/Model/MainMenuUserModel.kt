package com.cnunez.docufast.user.mainmenu.Model

import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.google.firebase.firestore.FirebaseFirestore

class MainMenuUserModel : MainMenuUserContract.Model {
    private val db = FirebaseFirestore.getInstance()

    override fun fetchUserGroups(userId: String, callback: (List<Group>?, String?) -> Unit) {
        db.collection("groups")
            .whereArrayContains("memberIds", userId)
            .get()
            .addOnSuccessListener { documents ->
                val groups = documents.map { it.toObject(Group::class.java) }
                callback(groups, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception.message)
            }
    }

    override fun fetchGroupFiles(groupId: String, callback: (List<File>?, String?) -> Unit) {
        db.collection("groups").document(groupId)
            .collection("files")
            .get()
            .addOnSuccessListener { documents ->
                val files = documents.map { it.toObject(File::class.java) }
                callback(files, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception.message)
            }
    }
}