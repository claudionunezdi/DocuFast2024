package com.cnunez.docufast.useCamera.Model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_files")
data class TextFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)