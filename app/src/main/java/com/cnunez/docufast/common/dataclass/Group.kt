package com.cnunez.docufast.common.dataclass

import java.io.File

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val members: MutableList<User> = mutableListOf(),
    val files: MutableList<File> = mutableListOf(),
)