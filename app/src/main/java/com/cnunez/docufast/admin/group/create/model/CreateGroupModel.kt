package com.cnunez.docufast.admin.group.create.model

import android.content.Context
import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

class CreateGroupModel(context: Context) : CreateGroupContract.Model {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    override fun saveGroup(
        group: Group,
        onSuccess: () -> Unit,
        onFailure: (exception: Exception) -> Unit
    ) {
        // Verificar permisos primero
        if (!isAdminUser()) {
            onFailure(Exception("User doesn't have admin permissions"))
            return
        }

        database.child("groups").child(group.id).setValue(group.toMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    override fun fetchUsersByOrganization(
        organization: String,
        onSuccess: (users: List<User>) -> Unit,
        onFailure: (exception: Exception) -> Unit
    ) {
        database.child("users")
            .orderByChild("organization")
            .equalTo(organization)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = mutableListOf<User>()
                    for (userSnapshot in snapshot.children) {
                        userSnapshot.getValue(User::class.java)?.let { users.add(it) }
                    }
                    onSuccess(users)
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(Exception(error.message))
                }
            })
    }

    override fun fetchAdminUser(
        userId: String,
        onSuccess: (user: User) -> Unit,
        onFailure: (exception: Exception) -> Unit
    ) {
        if (!isAdminUser()) {
            onFailure(Exception("User doesn't have admin permissions"))
            return
        }

        database.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let { onSuccess(it) } ?: onFailure(Exception("User not found"))
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(Exception(error.message))
                }
            })
    }

    private fun isAdminUser(): Boolean {
        return sharedPreferences.getString("userRole", null) == "admin"
    }

    // Extensión para convertir Group a Map (necesario para Realtime Database)
    private fun Group.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "members" to members.associate { it.id to true }, // Guarda solo IDs de miembros
            "createdAt" to System.currentTimeMillis()
        )
    }
}