package com.cnunez.docufast.common.utils

import com.cnunez.docufast.common.dataclass.File
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {

    fun saveTextFileToFirebase(
        db: FirebaseFirestore,
        textFile: File.TextFile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("organizations")
            .document(textFile.metadata.organizationId)
            .collection("files") // Colección unificada
            .document(textFile.id)
            .set(textFile.toFirebaseMap()) // Usar el mapa de Firebase
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun saveImageFileToFirebase(
        db: FirebaseFirestore,
        imageFile: File.ImageFile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("organizations")
            .document(imageFile.metadata.organizationId)
            .collection("files") // Colección unificada
            .document(imageFile.id)
            .set(imageFile.toFirebaseMap()) // Usar el mapa de Firebase
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Método genérico para cualquier tipo de archivo
    fun saveFileToFirebase(
        db: FirebaseFirestore,
        file: File,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("organizations")
            .document(file.metadata.organizationId)
            .collection("files")
            .document(file.id)
            .set(file.toFirebaseMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}