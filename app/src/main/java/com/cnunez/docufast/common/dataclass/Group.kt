package com.cnunez.docufast.common.dataclass

import android.os.Parcelable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Group(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    val organization: String = "",
    var members: Map<String, Boolean> = emptyMap(),
    var files: Map<String, Boolean> = emptyMap(),
    var createdAt: Long = System.currentTimeMillis()
) : Parcelable {

    constructor() : this("", "", "", "", emptyMap(), emptyMap())

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): Group {
            return Group(
                id = snapshot.key ?: "",
                name = snapshot.child("name").getValue(String::class.java) ?: "",
                description = snapshot.child("description").getValue(String::class.java) ?: "",
                organization = snapshot.child("organization").getValue(String::class.java) ?: "",
                members = parseMembers(snapshot.child("members")),
                files = parseFiles(snapshot.child("files")),
                createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
            )
        }

        private fun parseMembers(membersSnapshot: DataSnapshot): Map<String, Boolean> {
            return when {
                membersSnapshot.exists() -> {
                    try {
                        membersSnapshot.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})
                            ?: emptyMap()
                    } catch (e: Exception) {
                        // Manejar caso donde members es boolean (legacy)
                        if (membersSnapshot.getValue(Boolean::class.java) == true) {
                            emptyMap() // O podrías lanzar una excepción
                        } else {
                            emptyMap()
                        }
                    }
                }
                else -> emptyMap()
            }
        }

        private fun parseFiles(filesSnapshot: DataSnapshot): Map<String, Boolean> {
            return if (filesSnapshot.exists()) {
                try {
                    filesSnapshot.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {})
                        ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }
        }
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "organization" to organization,
        "members" to members, // Siempre devuelve Map, nunca Boolean
        "files" to files,
        "createdAt" to createdAt
    )

    // Métodos utilitarios mejorados
    fun isMember(userId: String): Boolean = members[userId] == true

    fun addMember(userId: String): Group {
        val newMembers = members.toMutableMap()
        newMembers[userId] = true
        return this.copy(members = newMembers)
    }

    fun removeMember(userId: String): Group {
        val newMembers = members.toMutableMap()
        newMembers.remove(userId)
        return this.copy(members = newMembers)
    }
}