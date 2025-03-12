package com.cnunez.docufast.admin.user.list.model

import com.cnunez.docufast.admin.user.list.contract.UserListContract
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.firestore.FirebaseFirestore

class UserListModel : UserListContract.Model {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun fetchUsers(organization: String, callback: (List<User>?, String?) -> Unit) {
        db.collection("users").whereEqualTo("organization", organization).get()
            .addOnSuccessListener { result ->
                val users = result.toObjects(User::class.java)
                callback(users, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception.message)
            }
    }

    override fun deleteUser(userId: String, callback: (Boolean, String?) -> Unit) {
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }
}