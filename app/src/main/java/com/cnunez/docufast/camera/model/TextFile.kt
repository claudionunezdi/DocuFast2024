package com.cnunez.docufast.camera.model

data class TextFile(
    val id: String? = null, // Cambiado a String
    val uri: String,
    val content: String,
    var fileName: String
)