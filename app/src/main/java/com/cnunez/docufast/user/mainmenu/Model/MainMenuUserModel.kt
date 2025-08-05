package com.cnunez.docufast.user.mainmenu.Model

import android.util.Log
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class MainMenuUserModel : MainMenuUserContract.Model {
    private val database = FirebaseDatabase.getInstance()

    override fun fetchUserGroups(userId: String, callback: (List<Group>?, String?) -> Unit) {
        Log.d("MODEL_DEBUG", "Iniciando carga de grupos para usuario: $userId")

        val userWorkGroupsRef = database.getReference("users/$userId/workGroups")

        userWorkGroupsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val groupIds = task.result?.children?.mapNotNull {
                    it.key
                } ?: emptyList()

                Log.d("MODEL_DEBUG", "IDs de grupos encontrados: $groupIds")

                if (groupIds.isEmpty()) {
                    Log.d("MODEL_DEBUG", "El usuario no tiene grupos asignados")
                    callback(emptyList(), "No tienes grupos asignados")
                    return@addOnCompleteListener
                }

                val groups = mutableListOf<Group>()
                val missingGroups = mutableListOf<String>()
                var completedCount = 0

                groupIds.forEach { groupId ->
                    database.getReference("groups/$groupId").get()
                        .addOnSuccessListener { groupSnapshot ->
                            completedCount++
                            if (groupSnapshot.exists()) {
                                try {
                                    val group = groupSnapshot.getValue<Group>()?.copy(id = groupId)
                                    if (group != null) {
                                        Log.d("MODEL_DEBUG", "Grupo cargado: ${group.name} (ID: ${group.id})")
                                        groups.add(group)
                                    } else {
                                        Log.e("MODEL_ERROR", "Error parseando grupo $groupId")
                                        missingGroups.add(groupId)
                                    }
                                } catch (e: Exception) {
                                    Log.e("MODEL_ERROR", "Error parseando grupo $groupId", e)
                                    missingGroups.add(groupId)
                                }
                            } else {
                                Log.d("MODEL_DEBUG", "Grupo $groupId no existe en la base de datos")
                                missingGroups.add(groupId)
                            }

                            if (completedCount == groupIds.size) {
                                if (groups.isNotEmpty()) {
                                    val warning = if (missingGroups.isNotEmpty()) {
                                        "Algunos grupos no se encontraron: ${missingGroups.joinToString()}"
                                    } else null
                                    callback(groups, warning)
                                } else {
                                    callback(null, "Ninguno de tus grupos existe en la base de datos")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            completedCount++
                            Log.e("MODEL_ERROR", "Error cargando grupo $groupId", e)
                            missingGroups.add(groupId)

                            if (completedCount == groupIds.size) {
                                if (groups.isNotEmpty()) {
                                    callback(groups, "Error cargando algunos grupos: ${missingGroups.joinToString()}")
                                } else {
                                    callback(null, "Error cargando todos los grupos")
                                }
                            }
                        }
                }
            } else {
                val errorMsg = task.exception?.message ?: "Error desconocido al obtener grupos"
                Log.e("MODEL_ERROR", errorMsg, task.exception)
                callback(null, errorMsg)
            }
        }
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