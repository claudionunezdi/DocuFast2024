package com.cnunez.docufast.common.firebase.storage

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

object FirebaseStorageManager {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    private val rtdb = FirebaseDatabase.getInstance().reference

    /**
     * Sube un archivo y guarda su referencia en RTDB
     * @param filePath Ruta local del archivo
     * @param fileName Nombre deseado del archivo
     * @param rtdbPath Ruta en RTDB para guardar la metadata (ej: "users/user123/files")
     * @param additionalData Datos adicionales para guardar en RTDB (debe usar tipos compatibles)
     * @param onProgress Callback con progreso (0-100)
     * @param onSuccess Callback con URL y ID del documento
     * @param onFailure Callback con error
     */
    fun uploadFileToRTDB(
        filePath: String,
        fileName: String,
        rtdbPath: String,
        additionalData: Map<String, Any?> = mapOf(),
        onProgress: (percentage: Double) -> Unit = {},
        onSuccess: (downloadUrl: String, rtdbId: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val file = File(filePath)
        val sanitizedName = fileName.replace("/", "_") // Prevenir paths inválidos
        val fileRef = storageRef.child("uploads/${System.currentTimeMillis()}_$sanitizedName")

        fileRef.putFile(Uri.fromFile(file))
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                onProgress(progress)
            }
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    // Crear objeto compatible con RTDB
                    val fileData = hashMapOf<String, Any?>(
                        "name" to fileName,
                        "url" to uri.toString(),
                        "uploadedAt" to System.currentTimeMillis(),
                        "size" to file.length(),
                        "type" to file.extension
                    )

                    // Filtrar additionalData para usar solo tipos compatibles
                    val compatibleAdditionalData = additionalData.filterValues { value ->
                        when (value) {
                            is String, is Number, is Boolean, is List<*>, is Map<*, *>, null -> true
                            else -> false
                        }
                    }

                    fileData.putAll(compatibleAdditionalData)

                    // Guardar en RTDB
                    val newRef = rtdb.child(rtdbPath).push()
                    newRef.setValue(fileData)
                        .addOnSuccessListener {
                            onSuccess(uri.toString(), newRef.key ?: "")
                        }
                        .addOnFailureListener(onFailure)
                }
            }
            .addOnFailureListener(onFailure)
    }

    // ... (deleteFileFromRTDB permanece igual)
}