package com.cnunez.docufast.admin.group.edit.model

import android.content.Context
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.admin.group.edit.contract.ListContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListModel(private val context: Context) : ListContract.Model {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun fetchGroups(callback: (List<Group>?, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(null, "User not authenticated")
            return
        }

        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("userRole", null)

        if (userRole == "admin") {
            db.collection("groups").get()
                .addOnSuccessListener { result ->
                    val groups = result.map { it.toObject(Group::class.java) }
                    callback(groups, null)
                }
                .addOnFailureListener { exception ->
                    callback(emptyList(), exception.message)
                }
        } else {
            callback(null, "User does not have admin permissions")
        }
    }

    override fun deleteGroup(groupId: String, callback: (Boolean, String?) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("userRole", null)

        if (userRole == "admin") {
            db.collection("groups").document(groupId).delete()
                .addOnSuccessListener {
                    callback(true, null)
                }
                .addOnFailureListener { exception ->
                    callback(false, exception.message)
                }
        } else {
            callback(false, "User does not have admin permissions")
        }
    }
}