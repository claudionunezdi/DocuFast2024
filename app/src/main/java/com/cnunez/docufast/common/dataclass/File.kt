package com.cnunez.docufast.common.dataclass

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class File(
    var id: String = "",               // push key
    var name: String = "",             // nombre amigable
    var creationDate: String = "",     // p.ej. ISO8601
    var createdBy: String = "",        // UID del usuario
    var groupId: String = "",          // ID del grupo
    var imageFile: ImageFile = ImageFile(),
    var textFile:  TextFile  = TextFile()
) {
    constructor() : this("", "", "", "", "", ImageFile(), TextFile())

    fun toMap(): Map<String, Any?> = mapOf(
        "id"           to id,
        "name"         to name,
        "creationDate" to creationDate,
        "createdBy"    to createdBy,
        "groupId"      to groupId,
        "imageFile"    to imageFile.toMap(),
        "textFile"     to textFile.toMap()
    )

    companion object {
        fun fromSnapshot(s: DataSnapshot): File {
            val imgSnap  = s.child("imageFile")
            val txtSnap  = s.child("textFile")
            return File(
                id           = s.key.orEmpty(),
                name         = s.child("name").getValue(String::class.java).orEmpty(),
                creationDate = s.child("creationDate").getValue(String::class.java).orEmpty(),
                createdBy    = s.child("createdBy").getValue(String::class.java).orEmpty(),
                groupId      = s.child("groupId").getValue(String::class.java).orEmpty(),
                imageFile    = ImageFile.fromSnapshot(imgSnap),
                textFile     = TextFile.fromSnapshot(txtSnap)
            )
        }
    }
}
