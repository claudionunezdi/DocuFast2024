package com.cnunez.docufast.user.mainmenu.Model

import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainMenuUserModel : MainMenuUserContract.Model {
    private val database = FirebaseDatabase.getInstance()

    override fun fetchUserGroups(userId: String, callback: (List<Group>?, String?) -> Unit) {
        val userGroupsRef = database.getReference("users/$userId/groups")

        userGroupsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupIds = snapshot.children.mapNotNull { it.key }
                val groups = mutableListOf<Group>()

                groupIds.forEach { groupId ->
                    database.getReference("groups/$groupId").addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(groupSnapshot: DataSnapshot) {
                                groupSnapshot.takeIf { it.exists() }?.let {
                                    groups.add(Group.fromSnapshot(it))
                                }

                                if (groups.size == groupIds.size) {
                                    callback(groups, null)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(null, error.message)
                            }
                        }
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, error.message)
            }
        })
    }

    override fun fetchGroupFiles(groupId: String, callback: (List<File>?, String?) -> Unit) {
        database.getReference("groups/$groupId/files")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val files = snapshot.children.mapNotNull {
                        it.getValue(File::class.java)?.copy(id = it.key ?: "")
                    }
                    callback(files, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, error.message)
                }
            })
    }
}