package com.cnunez.docufast.common.manager


import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class WorkGroupManager {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createWorkGroup(
        name: String,
        description: String,
        members: List<User>,
        files: List<File>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val workGroup = WorkGroup(
            id = name.hashCode(), // Generate an ID based on the name's hash code
            name = name,
            description = description,
            members = members,
            files = files
        )

        val workGroupData = hashMapOf(
            "name" to name,
            "description" to description,
            "members" to members.map { it.id },
            "files" to files.map { it.name }
        )

        db.collection("workGroups").document(workGroup.id.toString()).set(workGroupData)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun getWorkGroup(id: Int, onComplete: (WorkGroup?, String?) -> Unit) {
        db.collection("workGroups").document(id.toString()).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val members = document.get("members") as List<Int>
                    val files = document.get("files") as List<String>


                    val workGroup = WorkGroup(
                        id = id,
                        name = name,
                        description = description,
                        members = members.map { User(it) },
                        files = files.map { File(it) }
                    )
                    onComplete(workGroup, null)
                } else {
                    onComplete(null, "WorkGroup not found")
                }
            }
            .addOnFailureListener { e ->
                onComplete(null, e.message)
            }
    }

    fun deleteWorkGroup(id: Int, onComplete: (Boolean, String?) -> Unit) {
        db.collection("workGroups").document(id.toString()).delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }
}