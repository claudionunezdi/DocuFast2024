package com.cnunez.docufast.useCamera.Model

interface CameraModel {
    fun takePhoto(callback: (String?)->Unit)
}