package com.cnunez.docufast.camera.data

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class FileRepository {
    // Configuración EXPLÍCITA del bucket (usa tu URL exacta)
    private val storage = Firebase.storage("gs://docufast-7b4fa.firebasestorage.app")

    suspend fun uploadImage(
        bitmap: Bitmap,
        path: String
    ): Pair<String, String> {
        try {
            // 1. Validaciones
            require(!bitmap.isRecycled) { "Bitmap está reciclado" }
            require(path.isNotBlank()) { "Ruta inválida" }

            // 2. Prepara la referencia
            val fileRef = storage.reference.child(path.sanitizePath())

            // 3. Debug: Verifica la ruta completa
            Log.d("FIREBASE_DEBUG", """
                |Bucket: ${storage.reference.bucket}
                |Path completo: ${fileRef.path}
                |Tamaño de imagen: ${bitmap.byteCount} bytes
            """.trimMargin())

            // 4. Compresión y subida
            val bytes = ByteArrayOutputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)
                it.toByteArray()
            }

            fileRef.putBytes(bytes).await()
            val url = fileRef.downloadUrl.await().toString()

            Log.i("UPLOAD", "Imagen subida: $url")
            return fileRef.name to url

        } catch (e: Exception) {
            Log.e("UPLOAD_ERROR", "Error en uploadImage: ${e.javaClass.simpleName}", e)
            throw when (e) {
                is CancellationException -> e
                is IllegalArgumentException -> e
                else -> IOException("Error al subir imagen: ${e.message}", e)
            }
        }
    }

    suspend fun uploadText(
        content: String,
        path: String
    ): Pair<String, String> {
        try {
            // 1. Validaciones
            require(content.isNotEmpty()) { "Contenido vacío" }
            require(path.isNotBlank()) { "Ruta inválida" }

            // 2. Prepara la referencia
            val fileRef = storage.reference.child(path.sanitizePath())

            // 3. Debug
            Log.d("FIREBASE_DEBUG", "Subiendo texto a: ${fileRef.path}")

            // 4. Subida
            fileRef.putBytes(content.toByteArray(Charsets.UTF_8)).await()
            val url = fileRef.downloadUrl.await().toString()

            Log.i("UPLOAD", "Texto subido: $url")
            return fileRef.name to url

        } catch (e: Exception) {
            Log.e("UPLOAD_ERROR", "Error en uploadText: ${e.javaClass.simpleName}", e)
            throw when (e) {
                is CancellationException -> e
                is IllegalArgumentException -> e
                else -> IOException("Error al subir texto: ${e.message}", e)
            }
        }
    }

    // Extensión para limpieza de rutas
    private fun String.sanitizePath(): String {
        return this
            .replace("//", "/")
            .replace(Regex("[^a-zA-Z0-9_./-]"), "_")
            .trimStart('/')
    }
}