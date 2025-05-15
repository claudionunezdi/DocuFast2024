package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.camera.model.Photo
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class PhotoDaoFirebase(private val firebaseDatabase: FirebaseDatabase) {
    private val databaseReference = firebaseDatabase.getReference("photos")

    suspend fun insert(photo: Photo): String {
        val key = databaseReference.push().key ?: throw Exception("Error generating key")
        databaseReference.child(key).setValue(photo).await()
        return key
    }

    suspend fun getPhotoById(id: String): Photo? {
        val snapshot = databaseReference.child(id).get().await()
        return snapshot.getValue(Photo::class.java)
    }

    suspend fun getAllPhotos(): List<Photo> {
        val snapshot = databaseReference.get().await()
        return snapshot.children.mapNotNull { it.getValue(Photo::class.java) }
    }
}