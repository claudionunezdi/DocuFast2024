package com.cnunez.docufast.common.firebase.storage
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.io.File

fun uploadTextFileToFirebase(filePath: String, fileName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val fileRef = storageRef.child("textfiles/$fileName")

    val file = File(filePath)
    val uri = Uri.fromFile(file)

    fileRef.putFile(uri)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}