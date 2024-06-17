package com.cnunez.docufast.useCamera.Model.Impl

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.cnunez.docufast.useCamera.Contract.CameraContract
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraModelImpl(private val context: Context) : CameraContract.CameraModel {
    private var photoFile: File? = null

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            photoFile = this
        }
    }

    override fun takePhoto(callback: (Uri?) -> Unit) {
        try {
            val photoFile = createImageFile()
            val photoUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.fileprovider",
                photoFile
            )
            callback(photoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }

    override fun applyOcr(photoUri: Uri, callback: (String?) -> Unit) {
        try {
            val image = InputImage.fromFilePath(context, photoUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    callback(visionText.text)
                }
                .addOnFailureListener { e ->
                    callback(null)
                }
        } catch (e: Exception) {
            callback(null)
        }
    }

    override fun saveTextToFile(text: String, callback: (Boolean, String?) -> Unit) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "OCR_${timeStamp}.txt"
        val storageDir: File? = context.getExternalFilesDir(null)
        val textFile = File(storageDir, fileName)

        try {
            FileOutputStream(textFile).use { outputStream ->
                outputStream.write(text.toByteArray())
            }
            callback(true, textFile.absolutePath)
        } catch (e: IOException) {
            callback(false, e.message)
        }
    }
}
