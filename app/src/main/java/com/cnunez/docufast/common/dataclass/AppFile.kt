package com.cnunez.docufast.common.dataclass

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppFile(
    var id: String = "",
    var name: String = "",
    var creationDate: String = "",
    var photoUrl: String = "",
    var extractedText: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "creationDate" to creationDate,
        "photoUrl" to photoUrl,
        "extractedText" to extractedText
    )

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): AppFile {
            return AppFile(
                id = snapshot.key.orEmpty(),
                name = snapshot.child("name").getValue(String::class.java).orEmpty(),
                creationDate = snapshot.child("creationDate").getValue(String::class.java).orEmpty(),
                photoUrl = snapshot.child("photoUrl").getValue(String::class.java).orEmpty(),
                extractedText = snapshot.child("extractedText").getValue(String::class.java).orEmpty()
            )
        }
    }
}
