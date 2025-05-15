package com.cnunez.docufast.common.utils

import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.common.dataclass.ImageFile
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {

    fun saveTextFileToFirebase(db: FirebaseFirestore, textFile: TextFile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("organizations").document(textFile.organizationId)
            .collection("textFiles").document(textFile.id)
            .set(textFile)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun saveImageFileToFirebase(db: FirebaseFirestore, imageFile: ImageFile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("organizations").document(imageFile.organizationId)
            .collection("imageFiles").document(imageFile.id)
            .set(imageFile)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}