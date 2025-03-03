package com.cnunez.docufast.common.manager

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class GroupManager {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createGroup(
        name: String,
        description: String,
        members: List<User>,
        files: List<File>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val groupId = db.collection("groups").document().id
        val group = Group(
            id = groupId,
            name = name,
            description = description,
            members = members.toMutableList(), // Convert List to MutableList
            files = files.toMutableList() // Convert List to MutableList
        )

        val groupData = hashMapOf(
            "id" to groupId,
            "name" to name,
            "description" to description,
            "members" to members.map { it.id },
            "files" to files.map { it.name }
        )

        db.collection("groups").document(group.id).set(groupData)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun getGroup(id: String, onComplete: (Group?, String?) -> Unit) {
        db.collection("groups").document(id).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val members = document.get("members") as List<String>
                    val files = document.get("files") as List<String>

                    val group = Group(
                        id = id,
                        name = name,
                        description = description,
                        members = members.map { User(id = it) }.toMutableList(), // Convert List to MutableList
                        files = files.map { File(it) }.toMutableList() // Convert List to MutableList
                    )
                    onComplete(group, null)
                } else {
                    onComplete(null, "Group not found")
                }
            }
            .addOnFailureListener { e ->
                onComplete(null, e.message)
            }
    }

    fun deleteGroup(id: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection("groups").document(id).delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun getGroups(onComplete: (List<Group>, String?) -> Unit) {
        db.collection("groups").get()
            .addOnSuccessListener { result ->
                val groups = result.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val members = document.get("members") as List<String>
                    val files = document.get("files") as List<String>

                    Group(
                        id = id,
                        name = name,
                        description = description,
                        members = members.map { User(id = it) }.toMutableList(), // Convert List to MutableList
                        files = files.map { File(it) }.toMutableList() // Convert List to MutableList
                    )
                }
                onComplete(groups, null)
            }
            .addOnFailureListener { e ->
                onComplete(emptyList(), e.message)
            }
    }
}