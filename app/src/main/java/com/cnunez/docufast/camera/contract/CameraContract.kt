package com.cnunez.docufast.camera.contract

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile

interface CameraContract {
    interface View {
        fun showError(message: String)
        fun showPhoto(bitmap: Bitmap)
        fun showOcrResult(text: String)
        fun showFileSaved(textFile: TextFile)
        fun showImageSaved(imageFile: ImageFile)
        fun showEditFileNameDialog(fileId: String, callback: (String) -> Unit)
    }

    interface Presenter {
        fun capturePhoto()
        fun applyOcr(bitmap: Bitmap)
        fun saveOcrText(fileName: String)
        fun editTextFileName(fileId: String)
        fun analyze(imageProxy: ImageProxy)
    }

    interface Model {
        fun recognizeTextFromBitmap(bitmap: Bitmap, callback: (String?, String?) -> Unit)
        fun saveOcrText(text: String, fileName: String, groupId: String, onResult: (TextFile?, String?) -> Unit)
        fun saveImageToStorage(bitmap: Bitmap, groupId: String, onResult: (ImageFile?, String?) -> Unit)
    }
}
