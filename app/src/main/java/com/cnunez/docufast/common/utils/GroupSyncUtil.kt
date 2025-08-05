package com.cnunez.docufast.common.utils



import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object GroupSyncUtil {
    private val database = FirebaseDatabase.getInstance()

    suspend fun syncAllUsersGroups() {
        try {
            val users = database.getReference("users").get().await().children
            Log.d("GroupSync", "Usuarios a procesar: ${users.count()}")

            users.forEach { userSnapshot ->
                val userId = userSnapshot.key ?: return@forEach
                syncUserGroups(userId)
            }
        } catch (e: Exception) {
            Log.e("GroupSync", "Error general: ${e.message}")
        }
    }

    private suspend fun syncUserGroups(userId: String) {
        try {
            val updates = HashMap<String, Any>()
            val groups = database.getReference("groups")
                .orderByChild("members/$userId")
                .equalTo(true)
                .get()
                .await()

            groups.children.forEach { groupSnapshot ->
                val groupId = groupSnapshot.key ?: return@forEach
                updates["users/$userId/workGroups/$groupId"] = true
            }

            if (updates.isNotEmpty()) {
                database.reference.updateChildren(updates).await()
                Log.d("GroupSync", "Usuario $userId actualizado con ${updates.size} grupos")
            }
        } catch (e: Exception) {
            Log.e("GroupSync", "Error con usuario $userId: ${e.message}")
        }
    }
}