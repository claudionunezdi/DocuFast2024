package com.cnunez.docufast.common.dataclass

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.Gson

@IgnoreExtraProperties
data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var organization: String = "",
    var workGroups: Map<String, Boolean> = emptyMap(),
    var role: String = "",
    var stability: Int = 0,
    var createdAt: Long = 0L,
    var isSelected: Boolean = false
) : Parcelable {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", "", "", emptyMap(), "", 0, 0L, false)

    // Constructor Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        readWorkGroupsFromParcel(parcel),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readLong(),
        parcel.readByte() == 1.toByte()
    )

    // Método para convertir a JSON string
    fun toJsonString(): String {
        return Gson().toJson(this)
    }

    // Método para crear User desde JSON string
    companion object {
        private fun readWorkGroupsFromParcel(parcel: Parcel): Map<String, Boolean> {
            val size = parcel.readInt()
            return mutableMapOf<String, Boolean>().apply {
                for (i in 0 until size) {
                    put(parcel.readString() ?: "", parcel.readByte() == 1.toByte())
                }
            }
        }

        fun fromJsonString(json: String): User? {
            return try {
                Gson().fromJson(json, User::class.java)
            } catch (e: Exception) {
                null
            }
        }

        // Método alternativo para crear desde DataSnapshot (para Firebase)
        fun fromSnapshot(snapshot: com.google.firebase.database.DataSnapshot): User {
            return snapshot.getValue(User::class.java)?.apply {
                id = snapshot.key ?: ""
            } ?: User()
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }

        // Lista de roles válidos centralizada
        val VALID_ROLES = listOf("ADMIN", "USER")
    }

    // Método para convertir a Map (para Firebase)
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

    // Implementación Parcelable
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(organization)
        parcel.writeInt(workGroups.size)
        workGroups.forEach { (key, value) ->
            parcel.writeString(key)
            parcel.writeByte(if (value) 1 else 0)
        }
        parcel.writeString(role)
        parcel.writeInt(stability)
        parcel.writeLong(createdAt)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int = 0

    // Validación de rol
    fun isValidRole(): Boolean {
        return role.uppercase() in VALID_ROLES
    }

    // Métodos de conveniencia
    fun isAdmin(): Boolean = role.equals("ADMIN", ignoreCase = true)
    fun isUser(): Boolean = role.equals("USER", ignoreCase = true)
    fun belongsToOrganization(orgId: String): Boolean = organization == orgId
}