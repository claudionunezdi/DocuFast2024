// ImageFile.kt
package com.cnunez.docufast.common.dataclass

import android.os.Parcelable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
/**
 * Representa un archivo de imagen con metadatos para OCR usando Realtime Database.
 *
 * @property id               Clave push() generada por RTDB
 * @property uri              URI de la imagen (local o remota)
 * @property timestamp        Última modificación o marca de tiempo en milis
 * @property creationDate     Fecha de creación en formato ISO-8601
 * @property createdBy        UID del usuario que la creó
 * @property groupId          ID del grupo al que pertenece la imagen
 * @property organizationId   ID de la organización asociada
 */
data class ImageFile(
    var id: String = "",
    var uri: String = "",
    var timestamp: Long = 0L,
    var creationDate: String = "",
    var createdBy: String = "",
    var groupId: String = "",
    var organizationId: String = ""
) : Parcelable {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", 0L, "", "", "", "")

    /**
     * Convierte las propiedades de este objeto en un Map para guardar en RTDB.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id"             to id,
        "uri"            to uri,
        "timestamp"      to timestamp,
        "creationDate"   to creationDate,
        "createdBy"      to createdBy,
        "groupId"        to groupId,
        "organizationId" to organizationId
    )

    companion object {
        /**
         * Crea una instancia de ImageFile a partir de un DataSnapshot de Realtime Database.
         */
        fun fromSnapshot(snapshot: DataSnapshot): ImageFile = ImageFile(
            id             = snapshot.key.orEmpty(),
            uri            = snapshot.child("uri").getValue(String::class.java).orEmpty(),
            timestamp      = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
            creationDate   = snapshot.child("creationDate").getValue(String::class.java).orEmpty(),
            createdBy      = snapshot.child("createdBy").getValue(String::class.java).orEmpty(),
            groupId        = snapshot.child("groupId").getValue(String::class.java).orEmpty(),
            organizationId = snapshot.child("organizationId").getValue(String::class.java).orEmpty()
        )
    }
}
