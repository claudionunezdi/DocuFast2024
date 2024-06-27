package com.cnunez.docufast.camera.Model
import androidx.room.Database
import androidx.room.RoomDatabase




@Database(entities = [Photo::class, TextFile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun textFileDao(): TextFileDao
}
