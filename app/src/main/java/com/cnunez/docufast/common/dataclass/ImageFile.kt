// ImageFile.kt
package com.cnunez.docufast.common.dataclass

import android.os.Parcelable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
/**
 * Representa un archivo de imagen con metadatos para OCR usando Realtime Database.
 *
 * Hereda de la clase base File y añade propiedades específicas de imágenes.
 *
 * @property uri              URI de la imagen (local o remota)
 * @property timestamp        Última modificación o marca de tiempo en milis
 * @property width            Ancho de la imagen en píxeles (opcional)
 * @property height           Alto de la imagen en píxeles (opcional)
 * @property id               Clave push() generada por RTDB (heredado)
 * @property name             Nombre del archivo (heredado)
 * @property creationDate     Fecha de creación en formato ISO-8601 (heredado)
 * @property createdBy        UID del usuario que la creó (heredado)
 * @property groupId          ID del grupo al que pertenece (heredado)
 * @property organizationId   ID de la organización asociada (heredado)
 * @property storagePath      Ruta en Firebase Storage (heredado)
 * @property downloadUrl      URL pública de descarga (heredado)
 */
data class ImageFile(
    var uri: String = "",
    var timestamp: Long = 0L,
    var width: Int = 0,
    var height: Int = 0,
    override var id: String = "",
    override var name: String = "image_${System.currentTimeMillis()}",
    override var creationDate: String = "",
    override var createdBy: String = "",
    override var groupId: String = "",
    override var organizationId: String = "",
    override var storagePath: String = "",
    override var downloadUrl: String = ""
) : File(), Parcelable {

    // Constructor vacío requerido por Firebase
    constructor() : this("", 0L, 0, 0, "", "", "", "", "", "", "", "")

    /**
     * Convierte las propiedades de este objeto en un Map para guardar en RTDB.
     * Incluye tanto los campos heredados como los específicos de ImageFile.
     */
    override fun toMap(): Map<String, Any?> = super.toMap() + mapOf(
        "uri" to uri,
        "timestamp" to timestamp,
        "width" to width,
        "height" to height,
        "type" to getFileType().name  // Asegura el tipo para deserialización
    )

    @Exclude
    override fun getFileType(): FileType = FileType.IMAGE

    companion object {
        /**
         * Crea una instancia de ImageFile a partir de un DataSnapshot de Realtime Database.
         * Ahora usa el método de la clase base para el mapeo común.
         */
        fun fromSnapshot(snapshot: DataSnapshot): ImageFile {
            return snapshot.getValue(ImageFile::class.java)?.apply {
                id = snapshot.key ?: ""
            } ?: ImageFile()
        }
    }
}