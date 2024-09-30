package com.cnunez.docufast.camera.model

data class Photo(
    val id: Int = 0,
    val uri: String,
    val timestamp: Long = System.currentTimeMillis()
)