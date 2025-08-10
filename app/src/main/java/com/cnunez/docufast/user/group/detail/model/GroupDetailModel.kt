package com.cnunez.docufast.user.group.detail.model

import android.util.Log
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class GroupDetailModel : GroupDetailContract.Model {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance()

    override fun fetchGroupFiles(
        groupId: String,
        organizationId: String,
        filterType: FileType?,
        callback: (List<File>?, String?) -> Unit
    ) {
        val currentUser = auth.currentUser ?: run {
            callback(null, "Usuario no autenticado")
            return
        }

        // Verificar que el usuario pertenece a la organización
        database.child("users").child(currentUser.uid).child("organization").get()
            .addOnSuccessListener { orgSnapshot ->
                if (orgSnapshot.getValue(String::class.java) != organizationId) {
                    callback(null, "No tienes permisos en esta organización")
                    return@addOnSuccessListener
                }

                // Verificar membresía en el grupo
                database.child("groups").child(groupId).child("members").child(currentUser.uid).get()
                    .addOnSuccessListener { memberSnapshot ->
                        if (!memberSnapshot.exists()) {
                            callback(null, "No eres miembro de este grupo")
                            return@addOnSuccessListener
                        }

                        // Obtener archivos
                        database.child("files")
                            .orderByChild("metadata/groupId")
                            .equalTo(groupId)
                            .get()
                            .addOnSuccessListener { filesSnapshot ->
                                val files = mutableListOf<File>()
                                filesSnapshot.children.forEach { fileSnapshot ->
                                    File.fromSnapshot(fileSnapshot)?.let { file ->
                                        if (filterType == null || file.type == filterType) {
                                            files.add(file)
                                        }
                                    }
                                }
                                callback(files, null)
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
        val query = database.child("files")
            .orderByChild("metadata/groupId")
            .equalTo(groupId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val files = mutableListOf<File>()

                snapshot.children.forEach { fileSnapshot ->
                    File.fromSnapshot(fileSnapshot)?.takeIf { file ->
                        // Validación adicional de organización
                        file.metadata.organizationId == organizationId &&
                                (filterType == null || file.type == filterType)
                    }?.let { files.add(it) }
                }

                callback(files, null)
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
        if (file.storageInfo.path.isEmpty()) {
            callback(null, "El archivo no tiene ruta de almacenamiento")
            return
        }

        try {
            storage.getReference(file.storageInfo.path).downloadUrl
                .addOnSuccessListener { uri ->
                    callback(uri.toString(), null)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting download URL", e)
                    callback(null, "Error obteniendo URL: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Invalid storage path", e)
            callback(null, "Ruta de almacenamiento inválida")
        }
    }

    companion object {
        private const val TAG = "GroupDetailModel"
    }
}