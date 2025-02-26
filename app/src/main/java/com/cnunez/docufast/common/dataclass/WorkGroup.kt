package com.cnunez.docufast.common.dataclass

import java.io.File

data class WorkGroup(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val members: List<User> = emptyList(),
    val files: List<File> = emptyList()
) {
    constructor() : this(0, "", "", emptyList(), emptyList())
}