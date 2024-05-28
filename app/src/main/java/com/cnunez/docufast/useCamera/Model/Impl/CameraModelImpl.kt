package com.cnunez.docufast.useCamera.Model.Impl

import com.cnunez.docufast.useCamera.Model.CameraModel

class CameraModelImpl : CameraModel {
    override fun takePhoto(callback: (String?)-> Unit){
        val photoPath = "/sdcard/photo.jpg"
        callback(photoPath)
    }
}