// TextFile.kt
package com.cnunez.docufast.common.dataclass

import android.os.Parcelable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
/**
 * Representa el archivo de texto extraído vía OCR, con metadatos.
 *
 * @property id              Identificador único (clave push en RTDB)
 * @property imageFileId     ID de la ImageFile de la que proviene el texto
 * @property content         Texto extraído
 * @property fileName        Nombre del archivo .txt
 * @property created         Timestamp (ms) de creación del archivo físico
 * @property organizationId  ID de la organización asociada
 * @property groupId         ID del grupo al que pertenece
 * @property localPath       Ruta absoluta en el dispositivo donde se guardó el .txt
 * @property timestamp       Marca de tiempo (ms) para ordenaciones, idéntico a created
 */
data class TextFile(
    var id: String = "",
    var imageFileId: String = "",
    var content: String = "",
    var fileName: String = "",
    var created: Long = 0L,
    var organizationId: String = "",
    var groupId: String = "",
    var localPath: String = "",
    var timestamp: Long = 0L
) : Parcelable {
    constructor() : this("", "", "", "", 0L, "", "", "", 0L)

    /**
     * Convierte las propiedades en un Map para almacenar en Realtime Database.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id"             to id,
        "imageFileId"    to imageFileId,
        "content"        to content,
        "fileName"       to fileName,
        "created"        to created,
        "organizationId" to organizationId,
        "groupId"        to groupId,
        "localPath"      to localPath,
        "timestamp"      to timestamp
    )

    companion object {
        /**
         * Crea una instancia de TextFile a partir de un DataSnapshot de RTDB.
         */
        fun fromSnapshot(snapshot: DataSnapshot): TextFile = TextFile(
            id             = snapshot.key.orEmpty(),
            imageFileId    = snapshot.child("imageFileId").getValue(String::class.java).orEmpty(),
            content        = snapshot.child("content").getValue(String::class.java).orEmpty(),
            fileName       = snapshot.child("fileName").getValue(String::class.java).orEmpty(),
            created        = snapshot.child("created").getValue(Long::class.java) ?: 0L,
            organizationId = snapshot.child("organizationId").getValue(String::class.java).orEmpty(),
            groupId        = snapshot.child("groupId").getValue(String::class.java).orEmpty(),
            localPath      = snapshot.child("localPath").getValue(String::class.java).orEmpty(),
            timestamp      = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
        )
    }
}
