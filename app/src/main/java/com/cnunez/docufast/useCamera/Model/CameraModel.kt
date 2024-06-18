package com.cnunez.docufast.useCamera.Model

import android.net.Uri

import com.cnunez.docufast.useCamera.Model.Impl.CameraModelImpl

interface CameraModel {
    fun savePhoto()
    fun takePhoto(callback: (Uri?) -> Unit)
    fun applyOcr(photoUri: Uri, callback: (String?) -> Unit)
    fun saveTextToFile(text: String, callback: (Boolean, String?) -> Unit)
    fun showPhotoTaken(photoPath: String)
}