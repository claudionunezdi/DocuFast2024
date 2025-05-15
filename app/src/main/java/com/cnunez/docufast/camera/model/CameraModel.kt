package com.cnunez.docufast.camera.model

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.common.firebase.PhotoDaoFirebase
import com.cnunez.docufast.common.firebase.TextFileDaoFirebase
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraModel(
    private val activity: Activity,
    firebaseDatabase: FirebaseDatabase
) : CameraContract.CameraModel {
    private var photoFile: File? = null // Declaración de photoFile

    val photoDao = PhotoDaoFirebase(firebaseDatabase)
    val textFileDao = TextFileDaoFirebase(firebaseDatabase)

    // Métodos del modelo

    override fun takePhoto(callback: (Uri?) -> Unit) {
        try {
            Log.d("CameraModel", "takePhoto() called")

            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                activity,
                "${activity.applicationContext.packageName}.fileprovider",
                photoFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.grantUriPermission(activity.packageName, photoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            activity.startActivityForResult(intent, REQUEST_CODE)
            print("Callback photoUri")

            callback(photoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }

    override fun savePhoto() {
        val photoUri = photoFile?.let {
            FileProvider.getUriForFile(
                activity,
                "${activity.applicationContext.packageName}.fileprovider",
                it
            )
        }
        val resolver = activity.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, photoFile?.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        resolver.openOutputStream(imageUri!!).use { outputStream ->
            resolver.openInputStream(photoUri!!).use { inputStream ->
                if (outputStream != null) {
                    inputStream?.copyTo(outputStream)
                }
            }
        }
    }

    override fun showPhotoTaken(photoPath: String) {
        Toast.makeText(activity, "Photo taken: $photoPath", Toast.LENGTH_SHORT).show()
    }

    override fun applyOcr(photoUri: Uri, callback: (String?) -> Unit) {
        try {
            val inputStream = activity.contentResolver.openInputStream(photoUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            Log.d("CameraModel", "applyOcr() called")
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    callback(visionText.text)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    callback(null)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }

    override fun saveTextToFile(text: String, callback: (Boolean, String?) -> Unit) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "OCR_${timeStamp}.txt"
        val storageDir: File? = activity.getExternalFilesDir(null)
        val textFile = File(storageDir, fileName)

        try {
            FileOutputStream(textFile).use { outputStream ->
                outputStream.write(text.toByteArray())
            }
            callback(true, textFile.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
            callback(false, e.message)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = activity.getExternalFilesDir("Captured_images")
        if (!storageDir!!.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {
            photoFile = this
        }
    }

    companion object {
        const val REQUEST_CODE = 1
    }
}