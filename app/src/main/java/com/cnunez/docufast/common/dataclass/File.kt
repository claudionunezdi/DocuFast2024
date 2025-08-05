package com.cnunez.docufast.common.dataclass


import android.os.Parcelable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
sealed class File(
    open var id: String = "",
    open var name: String = "",
    open var creationDate: String = "",
    open var createdBy: String = "",
    open var groupId: String = "",
    open var organizationId: String = "",
    open var storagePath: String = "", // Nueva: Ruta en Firebase Storage
    open var downloadUrl: String = ""  // Nueva: URL pública del archivo
) : Parcelable {

    @Exclude
    abstract fun getFileType(): FileType

    abstract fun toMap(): Map<String, Any?>

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): File? {
            return when (snapshot.child("type").getValue(String::class.java)) {
                FileType.IMAGE.name -> snapshot.getValue(ImageFile::class.java)
                FileType.TEXT.name -> snapshot.getValue(TextFile::class.java)
                else -> null
            }?.apply {
                id = snapshot.key ?: ""
            }
        }
    }
}

enum class FileType { IMAGE, TEXT, PDF, AUDIO, VIDEO }

// ------------------------- Implementaciones Concretas -------------------------

@Parcelize
@IgnoreExtraProperties
data class ImageFile(
    var uri: String = "",
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

    @Exclude
    override fun getFileType() = FileType.IMAGE

    override fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "type" to getFileType().name,
        "creationDate" to creationDate,
        "createdBy" to createdBy,
        "groupId" to groupId,
        "organizationId" to organizationId,
        "storagePath" to storagePath,
        "downloadUrl" to downloadUrl,
        "uri" to uri,
        "width" to width,
        "height" to height
    )

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): ImageFile {
            return snapshot.getValue(ImageFile::class.java) ?: ImageFile()
        }
    }
}

@Parcelize
@IgnoreExtraProperties
data class TextFile(
    var content: String = "",
    var language: String = "es",
    override var id: String = "",
    override var name: String = "doc_${System.currentTimeMillis()}",
    override var creationDate: String = "",
    override var createdBy: String = "",
    override var groupId: String = "",
    override var organizationId: String = "",
    override var storagePath: String = "",
    override var downloadUrl: String = ""
) : File(), Parcelable {

    @Exclude
    override fun getFileType() = FileType.TEXT

    override fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "type" to getFileType().name,
        "creationDate" to creationDate,
        "createdBy" to createdBy,
        "groupId" to groupId,
        "organizationId" to organizationId,
        "storagePath" to storagePath,
        "downloadUrl" to downloadUrl,
        "content" to content,
        "language" to language
    )

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): TextFile {
            return snapshot.getValue(TextFile::class.java) ?: TextFile()
        }
    }
}