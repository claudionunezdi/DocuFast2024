package com.cnunez.docufast.camera.model

data class TextFile(
    val id: Int = 0,
    var fileName: String,
    val uri: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)