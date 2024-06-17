package com.cnunez.docufast.useCamera.Model

import android.net.Uri

interface CameraModel {

    fun takePhoto(callback: (Uri?)->Unit)
    fun applyOcr(photoUri: Uri, callback: (String?)->Unit)
    fun saveTextToFile(text: String, callback: (Boolean, String?)->Unit)
    fun clear()

}