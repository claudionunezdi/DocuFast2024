package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.camera.model.Photo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PhotoDaoFirebase {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("photos")

    suspend fun insert(photo: Photo): String {
        val document = collection.add(photo).await()
        return document.id
    }

    suspend fun getPhotoById(id: String): Photo? {
        val document = collection.document(id).get().await()
        return document.toObject(Photo::class.java)
    }

    suspend fun getAllPhotos(): List<Photo> {
        val snapshot = collection.get().await()
        return snapshot.toObjects(Photo::class.java)
    }
}