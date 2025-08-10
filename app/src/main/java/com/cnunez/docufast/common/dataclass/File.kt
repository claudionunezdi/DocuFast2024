package com.cnunez.docufast.common.dataclass

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@IgnoreExtraProperties
    sealed class File : Parcelable {
        abstract val id: String
        abstract val name: String
        abstract val type: FileType
        abstract val metadata: FileMetadata  // Contiene organizationId y creationDate
        abstract val storageInfo: StorageInfo

        @Exclude
        abstract fun toFirebaseMap(): Map<String, Any?>

        @Parcelize
        @IgnoreExtraProperties
        data class FileMetadata(
            val createdBy: String = "",  // Valor por defecto añadido
            val creatorName: String = "",
            val groupId: String = "",    // Valor por defecto añadido
            val organizationId: String = "", // Valor por defecto añadido
            val creationDate: String = currentFormattedDate(),
            val lastModified: String = currentFormattedDate()
        ) : Parcelable

    @Parcelize
    @IgnoreExtraProperties
    data class StorageInfo(
        val path: String = "",  // Valor por defecto
        val downloadUrl: String = "", // Valor por defecto
        val uri: String? = null
    ) : Parcelable

    // Implementaciones específicas
    @Parcelize
    @IgnoreExtraProperties
    data class ImageFile(
        override val id: String = "",
        override val name: String = "",
        override val metadata: FileMetadata = FileMetadata("", "", ""),
        override val storageInfo: StorageInfo = StorageInfo("", ""),
        val dimensions: Dimensions = Dimensions(0, 0, 0),
        val linkedOcrTextId: String? = null,
        val properties: ImageProperties = ImageProperties()
    ) : File() {
        override val type: FileType = FileType.IMAGE

        @Exclude
        override fun toFirebaseMap(): Map<String, Any?> = mapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "metadata" to metadata.toMap(),
            "storageInfo" to storageInfo.toMap(),
            "dimensions" to dimensions.toMap(),
            "linkedOcrTextId" to linkedOcrTextId,
            "properties" to properties.toMap(),

        )

        @Parcelize
        @IgnoreExtraProperties
        data class Dimensions(
            val width: Int = 0,  // Valor por defecto
            val height: Int = 0, // Valor por defecto
            val fileSize: Long = 0L // Valor por defecto
        ) : Parcelable {
            fun toMap() = mapOf(
                "width" to width,
                "height" to height,
                "fileSize" to fileSize
            )
        }

        @Parcelize
        @IgnoreExtraProperties

        data class ImageProperties(
            val isProcessed: Boolean = false,
            val hasText: Boolean = false,
            val dominantColor: String? = null
        ) : Parcelable {
            fun toMap() = mapOf(
                "isProcessed" to isProcessed,
                "hasText" to hasText,
                "dominantColor" to dominantColor
            )
        }
    }


    @Parcelize
    @IgnoreExtraProperties

    data class TextFile(
        override val id: String = "",
        override val name: String = "",
        override val metadata: FileMetadata = FileMetadata("", "", ""),
        override val storageInfo: StorageInfo = StorageInfo("", ""),
        val content: String = "",
        val sourceImageId: String? = null,  // Para OCR
        val ocrData: OcrMetadata? = null,   // Datos específicos de OCR
        val language: String = "es"



    ) : File() {
        override val type: FileType = FileType.TEXT

        @Exclude
        override fun toFirebaseMap(): Map<String, Any?> = mapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "metadata" to metadata.toMap(),
            "storageInfo" to storageInfo.toMap(),
            "content" to content,
            "sourceImageId" to sourceImageId,
            "ocrData" to ocrData?.toMap(),
            "language" to language,


        )

        @Parcelize
        @IgnoreExtraProperties
        data class OcrMetadata(
            val confidence: Float = 0f,
            val engine: String = "ML Kit",
            val processingTimeMs: Long = 0L,
            val rawOutput: String? = null
        ) : Parcelable {
            fun toMap() = mapOf(
                "confidence" to confidence,
                "engine" to engine,
                "processingTimeMs" to processingTimeMs,
                "rawOutput" to rawOutput
            )
        }
    }

    @Parcelize
    data class OcrResultFile(
        override val id: String = "",
        override val name: String = "",
        override val metadata: FileMetadata = FileMetadata(),
        override val storageInfo: StorageInfo = StorageInfo(),
        val originalImage: ImageReference = ImageReference(),
        val extractedText: String = "",
        val confidence: Float = 0f,
        val processingMetadata: ProcessingMetadata = ProcessingMetadata()

    ) : File() {
        override val type: FileType = FileType.OCR_RESULT

        @Exclude
        override fun toFirebaseMap(): Map<String, Any?> = mapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "metadata" to metadata.toMap(),
            "storageInfo" to storageInfo.toMap(),
            "originalImage" to originalImage.toMap(),
            "extractedText" to extractedText,
            "confidence" to confidence,
            "processingMetadata" to processingMetadata.toMap()
        )

        @Parcelize
        @IgnoreExtraProperties
        data class ImageReference(
            val imageId: String = "",
            val downloadUrl: String = ""
        ) : Parcelable {
            fun toMap() = mapOf(
                "imageId" to imageId,
                "downloadUrl" to downloadUrl
            )
        }

        @Parcelize
        @IgnoreExtraProperties
        data class ProcessingMetadata(
            val timestamp: Long = 0L,
            val engineVersion: String = "",
            val languageDetected: String = ""
        ) : Parcelable {
            fun toMap() = mapOf(
                "timestamp" to timestamp,
                "engineVersion" to engineVersion,
                "languageDetected" to languageDetected
            )
        }
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

        fun fromSnapshot(snapshot: DataSnapshot): File? {
            return when (snapshot.child("type").getValue(String::class.java)) {
                FileType.IMAGE.name -> snapshot.getValue(ImageFile::class.java)
                FileType.TEXT.name -> snapshot.getValue(TextFile::class.java)
                FileType.OCR_RESULT.name -> snapshot.getValue(OcrResultFile::class.java)
                else -> null
            }
        }

        private fun currentFormattedDate(): String {
            return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        }

        // Extensiones para mapeo
        private fun FileMetadata.toMap() = mapOf(
            "createdBy" to createdBy,
            "groupId" to groupId,
            "organizationId" to organizationId,
            "creationDate" to creationDate,
            "lastModified" to lastModified
        )

        private fun StorageInfo.toMap() = mapOf(
            "path" to path,
            "downloadUrl" to downloadUrl,
            "uri" to uri
        )
    }
}

enum class FileType {
    IMAGE, TEXT, PDF, AUDIO, VIDEO, OCR_RESULT
}