package com.cnunez.docufast.user.mainmenu.Model

import android.util.Log
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.google.firebase.database.FirebaseDatabase

class MainMenuUserModel : MainMenuUserContract.Model {
    private val db = FirebaseDatabase.getInstance().reference

    override fun fetchUserGroups(userId: String, callback: (List<Group>?, String?) -> Unit) {
        Log.d("MODEL_DEBUG", "Cargando grupos para uid=$userId")

        db.child("users").child(userId).get()
            .addOnSuccessListener { userSnap ->
                if (!userSnap.exists()) {
                    callback(emptyList(), "Usuario no encontrado")
                    return@addOnSuccessListener
                }

                val orgId = userSnap.child("organization").getValue(String::class.java).orEmpty()
                val workGroupsSnap = userSnap.child("workGroups")

                // Soporta dos formatos de workGroups:
                // A) plano: users/{uid}/workGroups/{groupId}: true
                // B) anidado: users/{uid}/workGroups/{orgId}/{groupId}: true
                val groupIds = mutableListOf<String>()
                if (workGroupsSnap.hasChildren()) {
                    val looksFlat = workGroupsSnap.children.any { it.getValue(Boolean::class.java) != null }
                    if (looksFlat) {
                        workGroupsSnap.children.forEach { ch ->
                            if (ch.getValue(Boolean::class.java) == true) {
                                ch.key?.let { groupIds += it }
                            }
                        }
                    } else {
                        // anidado por organización
                        val orgNode = if (orgId.isNotEmpty() && workGroupsSnap.hasChild(orgId))
                            workGroupsSnap.child(orgId)
                        else
                            workGroupsSnap.children.firstOrNull()

                        orgNode?.children?.forEach { ch ->
                            if (ch.getValue(Boolean::class.java) == true) {
                                ch.key?.let { groupIds += it }
                            }
                        }
                    }
                }

                if (groupIds.isEmpty()) {
                    callback(emptyList(), "No tienes grupos asignados")
                    return@addOnSuccessListener
                }

                val result = mutableListOf<Group>()
                val missing = mutableListOf<String>()
                var pending = groupIds.size

                fun done() {
                    if (--pending == 0) {
                        callback(
                            result,
                            if (missing.isNotEmpty()) "Algunos grupos no se encontraron: ${missing.joinToString()}" else null
                        )
                    }
                }

                groupIds.forEach { gid ->
                    // 1) prueba /groups/{gid}
                    db.child("groups").child(gid).get()
                        .addOnSuccessListener { gSnap ->
                            if (gSnap.exists()) {
                                val g = Group.fromSnapshot(gSnap).copy(id = gid)
                                result += g
                                done()
                            } else {
                                // 2) prueba /organizations/{orgId}/groups/{gid}
                                if (orgId.isEmpty()) {
                                    missing += gid
                                    done()
                                } else {
                                    db.child("organizations").child(orgId)
                                        .child("groups").child(gid).get()
                                        .addOnSuccessListener { g2 ->
                                            if (g2.exists()) {
                                                var g = Group.fromSnapshot(g2).copy(id = gid)
                                                if (g.organization.isEmpty()) {
                                                    g = g.copy(organization = orgId)
                                                }
                                                result += g
                                            } else {
                                                missing += gid
                                            }
                                            done()
                                        }
                                        .addOnFailureListener {
                                            missing += gid
                                            done()
                                        }
                                }
                            }
                        }
                        .addOnFailureListener {
                            missing += gid
                            done()
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(null, e.message ?: "Error cargando datos de usuario")
            }
    }

    // Opcional: si en algún momento usas esto para previsualizar archivos desde el menú,
    // recuerda que groups/{groupId}/files es un mapa de IDs -> true.
    override fun fetchGroupFiles(groupId: String, callback: (List<File>?, String?) -> Unit) {
        db.child("groups").child(groupId).child("files").get()
            .addOnSuccessListener { mapSnap ->
                if (!mapSnap.exists()) {
                    callback(emptyList(), null)
                    return@addOnSuccessListener
                }
                val ids = mapSnap.children.mapNotNull { it.key }
                if (ids.isEmpty()) {
                    callback(emptyList(), null)
                    return@addOnSuccessListener
                }

                val files = mutableListOf<File>()
                var pending = ids.size

                fun done() {
                    if (--pending == 0) callback(files, null)
                }

                ids.forEach { fid ->
                    db.child("files").child(fid).get()
                        .addOnSuccessListener { fSnap ->
                            File.fromSnapshot(fSnap)?.let { files += it }
                            done()
                        }
                        .addOnFailureListener { _ -> done() }
                }
            }
            .addOnFailureListener { e -> callback(null, e.message) }
    }
}
