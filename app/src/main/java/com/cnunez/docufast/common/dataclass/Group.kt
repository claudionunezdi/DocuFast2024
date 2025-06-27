package com.cnunez.docufast.common.dataclass

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Group(
    var id: String = "", // Opcional para lógica local
    var name: String = "",
    var description: String = "",
    var members: Map<String, Boolean> = emptyMap(),
    var files: Map<String, Boolean> = emptyMap()
) {

    constructor() : this("", "", "", emptyMap(), emptyMap())

    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "members" to members,
        "files" to files
        // "id" excluido intencionalmente, ya que la clave es el nodo padre en Firebase
    )

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): Group {
            return Group(
                id = snapshot.key.orEmpty(),
                name = snapshot.child("name").getValue(String::class.java).orEmpty(),
                description = snapshot.child("description").getValue(String::class.java).orEmpty(),
                members = snapshot.child("members").getValue(
                    object : com.google.firebase.database.GenericTypeIndicator<Map<String, Boolean>>() {}
                ) ?: emptyMap(),
                files = snapshot.child("files").getValue(
                    object : com.google.firebase.database.GenericTypeIndicator<Map<String, Boolean>>() {}
                ) ?: emptyMap()
            )
        }
    }
}
