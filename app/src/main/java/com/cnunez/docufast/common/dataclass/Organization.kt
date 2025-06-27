// Organization.kt
/* package com.cnunez.docufast.common.dataclass

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Organization(
    var id: String = "",       // igual al orgId que uses en DB
    var name: String = "",     // nombre “visible” de la organización
    var hasAdmin: Boolean = false
) {
    constructor() : this("", "", false)

    fun toMap(): Map<String, Any?> = mapOf(
        "id"       to id,
        "name"     to name,
        "hasAdmin" to hasAdmin
    )

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): Organization {
            return Organization(
                id = snapshot.key.orEmpty(),
                name = snapshot.child("name").getValue(String::class.java).orEmpty(),
                hasAdmin = snapshot.child("hasAdmin").getValue(Boolean::class.java) ?: false
            )
        }
    }
}
*/