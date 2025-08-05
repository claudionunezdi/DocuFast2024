package com.cnunez.docufast.camera.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.cnunez.docufast.common.firebase.storage.FirebaseStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

import java.util.*



class CameraModel(
    private val context: Context,
    private val database: FirebaseDatabase
) : CameraContract.Model {

    // ML Kit Text Recognizer (optimizado para latín)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Firebase Storage
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    // Formateador de fecha para nombres de archivo
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    override fun recognizeTextFromBitmap(
        bitmap: Bitmap,
        callback: (String?, String?) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isNotEmpty()) {
                    Log.d("OCR_SUCCESS", "Text extracted: ${extractedText.take(50)}...")
                    callback(extractedText, null)
                } else {
                    callback(null, "No text found in image")
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR_ERROR", "Recognition failed", e)
                callback(null, "OCR failed: ${e.localizedMessage}")
            }
    }

    override fun saveImageToStorage(
        bitmap: Bitmap,
        groupId: String,
        onResult: (ImageFile?, String?) -> Unit
    ) {
        val currentUser = getCurrentUserId() ?: run {
            onResult(null, "User not authenticated")
            return
        }

        val tempFile = convertBitmapToTempFile(bitmap) ?: run {
            onResult(null, "Failed to process image")
            return
        }

        val timestamp = timestampFormat.format(Date())
        val fileName = "IMG_${timestamp}_${UUID.randomUUID().toString().take(4)}.jpg"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = FirebaseStorageManager.uploadFileSuspend(
                    filePath = tempFile.absolutePath,
                    fileName = fileName,
                    rtdbPath = "groups/$groupId/images",
                    additionalData = mapOf(
                        "uploadedBy" to currentUser,
                        "groupId" to groupId,
                        "timestamp" to System.currentTimeMillis(),
                        "createdBy" to currentUser,
                        "creationDate" to timestamp,
                        "localPath" to tempFile.absolutePath,
                        "fileName" to fileName,
                        "groupId" to groupId,
                        "timestamp" to System.currentTimeMillis(),


                    )
                )

                when (result) {
                    is FirebaseStorageManager.UploadResult.Success -> {
                        // Asegurar los tipos con conversión explícita
                        val downloadUrl = result.downloadUrl.toString()
                        val rtdbId = result.rtdbId.toString()

                        val imageFile = ImageFile(
                            id = rtdbId,
                            uri = downloadUrl,
                            createdBy = currentUser,
                            creationDate = timestamp,
                            timestamp = System.currentTimeMillis(),
                            groupId = groupId,
                            organizationId = getCurrentOrganizationId() ?: ""
                        )
                        withContext(Dispatchers.Main) { onResult(imageFile, null) }
                    }
                    is FirebaseStorageManager.UploadResult.Error -> {
                        withContext(Dispatchers.Main) {
                            onResult(null, "Storage error: ${result.exception.message}")
                        }
                    }

                    is FirebaseStorageManager.UploadResult.Progress -> TODO()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null, "Unexpected error: ${e.message}")
                }
            } finally {
                tempFile.delete() // Limpiar archivo temporal
            }
        }
    }

    override fun saveOcrText(
        text: String,
        fileName: String,
        groupId: String,
        organizationId: String,
        imageFileId: String,
        onResult: (TextFile?, String?) -> Unit
    ) {
        val currentUser = getCurrentUserId() ?: run {
            onResult(null, "User not authenticated")
            return
        }

        // Crear objeto TextFile
        val textFile = TextFile(
            id = "", // Firebase generará ID automática
            imageFileId = imageFileId,
            content = text,
            fileName = if (fileName.endsWith(".txt")) fileName else "$fileName.txt",
            created = System.currentTimeMillis(),
            organizationId = organizationId,
            groupId = groupId,
            localPath = "", // Opcional: guardar ruta local si es necesario
            timestamp = System.currentTimeMillis()
        )

        // Guardar en Realtime Database
        val textFilesRef = database.reference.child("groups/$groupId/textFiles")
        val newTextRef = textFilesRef.push()

        newTextRef.setValue(textFile.toMap())
            .addOnSuccessListener {
                val savedFile = textFile.copy(id = newTextRef.key ?: "")
                Log.d("SAVE_TEXT", "Text file saved: ${savedFile.fileName}")
                onResult(savedFile, null)
            }
            .addOnFailureListener { e ->
                Log.e("SAVE_TEXT_ERROR", "Database write failed", e)
                onResult(null, "Failed to save text: ${e.localizedMessage}")
            }
    }

    /* Helpers */
    private fun getCurrentUserId(): String? {
        // Implementar según tu sistema de autenticación
        return FirebaseAuth.getInstance().currentUser?.uid
    }


    private suspend fun getCurrentOrganizationId(): String? {
        // Obtener el usuario actual desde Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return null

        // Referencia a la base de datos de usuarios
        val userRef = database.reference.child("users").child(userId)

        return try {
            // Obtener el snapshot de forma síncrona (usando await())
            val snapshot = userRef.get().await()

            // Convertir el snapshot a objeto User y obtener la organización
            snapshot.getValue(User::class.java)?.organization
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener organización del usuario", e)
            null
        }
    }
    private fun convertBitmapToTempFile(bitmap: Bitmap): File? {
        return try {
            val file = File.createTempFile("temp_img_", ".jpg", context.cacheDir)
            val outputStream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error converting bitmap to file", e)
            null
        }
    }


    companion object {
        private const val TAG = "CameraModel"
    }
}