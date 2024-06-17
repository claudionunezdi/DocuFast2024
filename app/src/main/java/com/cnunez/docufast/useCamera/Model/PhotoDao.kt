package com.cnunez.docufast.useCamera.Model



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: Photo): Long

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Int): Photo?

    @Query("SELECT * FROM photos")
    suspend fun getAllPhotos(): List<Photo>
}
