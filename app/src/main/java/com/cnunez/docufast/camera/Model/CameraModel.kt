package com.cnunez.docufast.camera.Model

import android.net.Uri

interface CameraModel {
    fun savePhoto()
    fun takePhoto(callback: (Uri?) -> Unit)
    fun applyOcr(photoUri: Uri, callback: (String?) -> Unit)
    fun saveTextToFile(text: String, callback: (Boolean, String?) -> Unit)
    fun showPhotoTaken(photoPath: String)
}