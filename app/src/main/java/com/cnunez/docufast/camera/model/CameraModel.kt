// CameraModel.kt
package com.cnunez.docufast.camera.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraModel(
    private val context: Context,
    private val database: FirebaseDatabase
) : CameraContract.Model {

    override fun recognizeTextFromBitmap(
        bitmap: Bitmap,
        callback: (String?, String?) -> Unit
    ) {
        // Aquí deberías integrar ML Kit si aún no lo haces
        // callback("Texto detectado", null) o callback(null, "Error...")
    }

    override fun saveOcrText(
        text: String,
        fileName: String,
        groupId: String,
        onResult: (TextFile?, String?) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onResult(null, "Usuario no autenticado")

        val timestamp = System.currentTimeMillis()

        val textFile = TextFile(
            imageFileId = "",
            content = text,
            fileName = fileName,
            created = timestamp,
            organizationId = "", // Asignar si es necesario
            groupId = groupId
        )

        val ref = database.reference.child("textFiles").push()
        ref.setValue(textFile)
            .addOnSuccessListener {
                onResult(textFile, null)
            }
            .addOnFailureListener { ex ->
                onResult(null, ex.message)
            }
    }

    override fun saveImageToStorage(
        bitmap: Bitmap,
        groupId: String,
        onResult: (ImageFile?, String?) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onResult(null, "Usuario no autenticado")

        val timestamp = System.currentTimeMillis()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            .format(Date(timestamp))

        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestamp))}.jpg"
        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uid/$fileName")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageFile = ImageFile(
                        id = storageRef.name,
                        uri = uri.toString(),
                        createdBy = uid,
                        creationDate = formattedDate,
                        timestamp = timestamp,
                        groupId = groupId,
                        organizationId = "" // Asignar si lo usas
                    )
                    onResult(imageFile, null)
                }
            }
            .addOnFailureListener { ex ->
                onResult(null, ex.message)
            }
    }
}
