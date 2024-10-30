package com.cnunez.docufast.common.dataclass

import java.io.File

data class WorkGroup(
    val id: Int,
    val name: String,
    val description: String,
    val members: List<User>,
    val files: List<String>
) {}

