package com.cnunez.docufast.useCamera.Model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TextFileDao {
    @Insert
    suspend fun insert(textFile: TextFile): Long

    @Query("SELECT * FROM text_files WHERE id = :id")
    suspend fun getTextFileById(id: Int): TextFile?

    @Query("SELECT * FROM text_files")
    suspend fun getAllTextFiles(): List<TextFile>

    @Query("SELECT * FROM text_files WHERE uri = :uri")
    suspend fun getTextFileByUri(uri: String): TextFile?

    @Update
    suspend fun update(textFile:TextFile)
}