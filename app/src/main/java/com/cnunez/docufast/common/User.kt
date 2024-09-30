package com.cnunez.docufast.common

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val organization: String
)