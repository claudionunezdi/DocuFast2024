package com.cnunez.docufast.admin.group.create.model

import android.content.Context
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.UUID

class CreateGroupModel(private val context: Context) {

    private val groups = mutableListOf<Group>()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createGroup(name: String, description: String, members: List<User>, files: List<File>): Group {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("userRole", null)

        if (userRole == "admin") {
            val groupId = UUID.randomUUID().toString()
            val group = Group(
                id = groupId,
                name = name,
                description = description,
                members = members.toMutableList(), // Convert List to MutableList
                files = files.toMutableList() // Convert List to MutableList
            )
            groups.add(group)

            val groupData = hashMapOf(
                "id" to groupId,
                "name" to name,
                "description" to description,
                "members" to members.map { it.id },
                "files" to files.map { it.name }
            )

            db.collection("groups").document(group.id).set(groupData)
                .addOnSuccessListener {
                    // Group created successfully
                }
                .addOnFailureListener { e ->
                    // Handle error
                }

            return group
        } else {
            throw Exception("User does not have admin permissions")
        }
    }

    fun getGroups(): List<Group> {
        return groups
    }
}