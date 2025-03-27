package com.cnunez.docufast.common.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class TextFile(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val organizationId: String = ""
) : Parcelable

