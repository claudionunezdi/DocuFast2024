package com.cnunez.docufast.common.dataclass

import android.os.Parcel
import android.os.Parcelable

data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val organization: String = "",
    val workGroups: MutableList<String> = mutableListOf(),
    val role: String = "", // "User" or "Admin"
    val users: MutableList<User> = mutableListOf() // Only used if role is "Admin"
) : Parcelable {
    // No-argument constructor for deserialization
    constructor() : this(0, "", "", "", "", mutableListOf(), "", mutableListOf())

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!.toMutableList(),
        parcel.readString()!!,
        mutableListOf<User>().apply {
            parcel.readList(this, User::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(organization)
        parcel.writeStringList(workGroups)
        parcel.writeString(role)
        parcel.writeList(users)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

    // Method to add a group to the user's workGroups
    fun addGroup(groupId: String) {
        if (!workGroups.contains(groupId)) {
            workGroups.add(groupId)
        }
    }
}