// src/main/java/com/cnunez/docufast/common/dataclass/User.kt
package com.cnunez.docufast.common.dataclass

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var organization: String = "",    // <--- Aquí debe estar
    var workGroups: Map<String, Boolean> = emptyMap(),
    var role: String = "",
    var stability: Int = 0,
    var createdAt: Long = 0L,
    var isSelected: Boolean = false
) {
    constructor() : this("", "", "", "", emptyMap(), "", 0, 0L, false)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "organization" to organization,
            "workGroups" to workGroups,
            "role" to role,
            "stability" to stability,
            "createdAt" to createdAt,
            "isSelected" to isSelected
        )
    }
}
