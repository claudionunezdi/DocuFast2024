// uploadImageToFirebase.kt
package com.cnunez.docufast.common.firebase.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.cnunez.docufast.common.dataclass.ImageFile

/**
 * Sube la imagen a Firebase Storage bajo "images/{imageFile.id}.jpg",
 * pero como ya guardamos metadatos en Realtime DB, solo exponemos la función sencilla.
 */
fun uploadImageToFirebase(
    localUri: Uri,
    imageFileName: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val storage = FirebaseStorage.getInstance()
    val fileRef = storage.reference.child("images/$imageFileName")
    fileRef.putFile(localUri)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { ex -> onFailure(ex) }
}
