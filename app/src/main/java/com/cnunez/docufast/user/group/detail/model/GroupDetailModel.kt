package com.cnunez.docufast.user.group.detail.model

import android.util.Log
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GroupDetailModel : GroupDetailContract.Model {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun fetchGroupFiles(
        groupId: String,
        organizationId: String,
        filterType: FileType?,
        callback: (List<File>?, String?) -> Unit
    ) {
        val user = auth.currentUser ?: return callback(null, "Usuario no autenticado")

        // 1) Valida organización del usuario
        db.child("users").child(user.uid).child("organization").get()
            .addOnSuccessListener { orgSnap ->
                val userOrg = orgSnap.getValue(String::class.java) ?: ""
                if (userOrg != organizationId) {
                    callback(null, "No tienes permisos en esta organización")
                    return@addOnSuccessListener
                }

                // 2) Verifica membresía: /groups/{groupId}/members/{uid} ORG fallback
                checkMembership(user.uid, groupId, organizationId) { isMember, err ->
                    if (err != null) {
                        callback(null, err); return@checkMembership
                    }
                    if (!isMember) {
                        callback(null, "No eres miembro de este grupo"); return@checkMembership
                    }

                    // 3) Carga archivos desde /files por metadata/groupId
                    db.child("files")
                        .orderByChild("metadata/groupId")
                        .equalTo(groupId)
                        .get()
                        .addOnSuccessListener { filesSnap ->
                            val list = filesSnap.children.mapNotNull { File.fromSnapshot(it) }
                                .filter { filterType == null || it.type == filterType }
                            callback(list, null)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error loading files", e)
                            callback(null, "Error al cargar archivos")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error verifying organization", e)
                callback(null, "Error de verificación")
            }
    }

    override fun observeGroupFiles(
        groupId: String,
        organizationId: String,
        filterType: FileType?,
        callback: (List<File>?, String?) -> Unit
    ) {
        // Observa SIEMPRE en /files por groupId (el seed guarda así)
        val q = db.child("files")
            .orderByChild("metadata/groupId")
            .equalTo(groupId)

        q.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { snap ->
                    File.fromSnapshot(snap)?.takeIf { f ->
                        f.metadata.organizationId == organizationId &&
                                (filterType == null || f.type == filterType)
                    }
                }
                callback(list, null)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(null, "Error en tiempo real: ${error.message}")
            }
        })
    }

    override fun getFileDownloadUrl(
        file: File,
        callback: (String?, String?) -> Unit
    ) {
        val path = file.storageInfo.path
        if (path.isBlank()) {
            callback(null, "El archivo no tiene ruta de almacenamiento")
            return
        }
        try {
            com.google.firebase.storage.FirebaseStorage.getInstance()
                .getReference(path)
                .downloadUrl
                .addOnSuccessListener { uri -> callback(uri.toString(), null) }
                .addOnFailureListener { e -> callback(null, "Error obteniendo URL: ${e.message}") }
        } catch (e: Exception) {
            callback(null, "Ruta de almacenamiento inválida")
        }
    }

    // --- Helpers ---

    private fun checkMembership(
        uid: String,
        groupId: String,
        organizationId: String,
        cb: (Boolean, String?) -> Unit
    ) {
        // A) /groups/{groupId}/members/{uid}
        db.child("groups").child(groupId).child("members").child(uid).get()
            .addOnSuccessListener { m1 ->
                if (m1.exists()) {
                    cb(true, null)
                } else {
                    // B) fallback: /organizations/{orgId}/groups/{groupId}/members/{uid}
                    db.child("organizations").child(organizationId)
                        .child("groups").child(groupId)
                        .child("members").child(uid).get()
                        .addOnSuccessListener { m2 -> cb(m2.exists(), null) }
                        .addOnFailureListener { e -> cb(false, e.message) }
                }
            }
            .addOnFailureListener { e -> cb(false, e.message) }
    }

    companion object { private const val TAG = "GroupDetailModel" }
}
