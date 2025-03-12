package com.cnunez.docufast.common.dataclass

import android.os.Parcel
import android.os.Parcelable

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val organization: String = "",
    val workGroups: MutableList<String> = mutableListOf(),
    val role: String = "",
    val stability: Int = 0,
    val users: MutableList<User> = mutableListOf()
) : Parcelable {
    val isSelected: Boolean = false


    constructor() : this("", "", "", "", "", mutableListOf(), "", 0, mutableListOf())

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!.toMutableList(),
        parcel.readString()!!,
        parcel.readInt(),
        mutableListOf<User>().apply {
            parcel.readList(this, User::class.java.classLoader)
        }

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(organization)
        parcel.writeStringList(workGroups)
        parcel.writeString(role)
        parcel.writeInt(stability)
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
}