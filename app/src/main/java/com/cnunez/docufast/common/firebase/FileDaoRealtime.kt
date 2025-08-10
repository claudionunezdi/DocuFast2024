package com.cnunez.docufast.common.firebase

import android.net.Uri
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.ImageFile
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.common.firebase.AppDatabase.fileDao
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FileDaoRealtime(
    private val db: FirebaseDatabase,
    private val storageManager: FileStorageManager
) {
    private val filesRef = db.getReference("files")
    private val imagesRef = db.getReference("images")
    private val textFilesRef = db.getReference("textFiles") // Mantenido por compatibilidad

    // -------------------- Operaciones CRUD Unificadas --------------------
    suspend fun uploadFileWithUri(file: File, localUri: Uri): String = withContext(Dispatchers.IO){
        try {
            // 1. Determinar ruta en Storage según tipo
            val storagePath = when (file) {
                is File.ImageFile -> "orgs/${file.metadata.organizationId}/images/${file.id}"
                is File.TextFile -> "orgs/${file.metadata.organizationId}/texts/${file.id}"
                else -> throw IllegalArgumentException("Tipo de archivo no soportado")
            }

            // 2. Subir a Storage y obtener URL
            val downloadUrl = storageManager.uploadFile(localUri, storagePath).toString()

            // 3. Crear archivo con metadatos actualizados
            val fileToUpload = when (file) {
                is File.ImageFile -> file.copy(
                    id = filesRef.push().key ?: throw Exception("Error generando ID"),
                    storageInfo = file.storageInfo.copy(
                        path = storagePath,
                        downloadUrl = downloadUrl
                    )
                )
                is File.TextFile -> file.copy(
                    id = filesRef.push().key ?: throw Exception("Error generando ID"),
                    storageInfo = file.storageInfo.copy(
                        path = storagePath,
                        downloadUrl = downloadUrl
                    )
                )
                else -> throw IllegalArgumentException("Tipo de archivo no soportado")
            }

            // 4. Guardar en Realtime DB
            filesRef.child(fileToUpload.id).setValue(fileToUpload.toFirebaseMap()).await()

            // 5. Actualizar referencia en grupo (opcional)
            db.getReference("groups/${fileToUpload.metadata.groupId}/files/${fileToUpload.id}")
                .setValue(true).await()

            fileToUpload.id
        } catch (e: Exception) {
            throw Exception("Error subiendo archivo: ${e.message}")
        }
    }

    // -------------------- Operaciones Específicas para OCR --------------------
    suspend fun saveOcrResult(
        imageFile: File.ImageFile,
        extractedText: String,
        confidence: Float,
        userId: String
    ): File.TextFile = withContext(Dispatchers.IO) {
        val textFile = File.TextFile(
            id = filesRef.push().key ?: throw Exception("Error generando ID"),
            name = "ocr_${imageFile.name}",
            metadata = File.FileMetadata(
                createdBy = userId,
                groupId = imageFile.metadata.groupId,
                organizationId = imageFile.metadata.organizationId
            ),
            storageInfo = File.StorageInfo(
                path = "ocr_results/${imageFile.id}/text.txt",
                downloadUrl = "" // Puede generarse después
            ),
            content = extractedText,
            sourceImageId = imageFile.id,
            ocrData = File.TextFile.OcrMetadata(
                confidence = confidence,
                processingTimeMs = System.currentTimeMillis()
            ),
            language = "es",


        )

        // Guardar en ambas referencias por compatibilidad
        filesRef.child(textFile.id).setValue(textFile.toFirebaseMap()).await()
        textFilesRef.child(textFile.id).setValue(textFile.toFirebaseMap()).await()

        // Actualizar referencia en la imagen original
        filesRef.child(imageFile.id).child("linkedTextId").setValue(textFile.id).await()

        textFile
    }


    // -------------------- Consultas Unificadas --------------------
    suspend fun getFileById(id: String): File? = withContext(Dispatchers.IO) {
        filesRef.child(id).get().await().let { snapshot ->
            File.fromSnapshot(snapshot)
        }
    }

    fun observeFilesByGroup(groupId: String): Flow<List<File>> = callbackFlow {
        val query = filesRef.orderByChild("metadata/groupId").equalTo(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val files = snapshot.children.mapNotNull { File.fromSnapshot(it) }
                trySend(files)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun getFilesByType(groupId: String, type: FileType): List<File> {
        return observeFilesByGroup(groupId)
            .first() // Obtiene el primer valor del Flow
            .filter { it.type == type }
    }
    suspend fun getFilesByUserId(userId: String): List<File> = withContext(Dispatchers.IO) {
        val query = filesRef.orderByChild("metadata/createdBy").equalTo(userId)
        val snapshot = query.get().await()
        snapshot.children.mapNotNull { File.fromSnapshot(it) }
    }



    // -------------------- Operaciones de Texto (Compatibilidad) --------------------
    suspend fun getTextFileById(id: String): File.TextFile? = withContext(Dispatchers.IO) {
        textFilesRef.child(id).get().await().let { snapshot ->
            File.fromSnapshot(snapshot) as? File.TextFile
        }
    }

    // -------------------- Helpers --------------------
    suspend fun getFileDownloadUrl(fileId: String): String? = withContext(Dispatchers.IO) {
        getFileById(fileId)?.storageInfo?.downloadUrl?.takeIf { it.isNotEmpty() }
            ?: run {
                val file = getFileById(fileId) ?: return@withContext null
                storageManager.getDownloadUrl(file.storageInfo.path).also { url ->
                    filesRef.child(fileId).child("storageInfo/downloadUrl").setValue(url).await()
                }
            }
    }

    suspend fun deleteFile(fileId: String, groupId: String) = withContext(Dispatchers.IO) {
        try {
            // 1. Eliminar de Storage
            getFileById(fileId)?.let { file ->
                storageManager.deleteFile(file.storageInfo.path)
            }

            // 2. Eliminar de Realtime DB
            filesRef.child(fileId).removeValue().await()
            textFilesRef.child(fileId).removeValue().await() // Por compatibilidad

            // 3. Eliminar referencia en grupo
            db.getReference("groups/$groupId/files/$fileId").removeValue().await()
        } catch (e: Exception) {
            throw Exception("Error eliminando archivo: ${e.message}")
        }
    }

    suspend fun getFilesByGroup(groupId: String): List<File> {
        return observeFilesByGroup(groupId).first()
    }


    ///CRUD PARA IMAGENES

    suspend fun insertImage(imageFile: File.ImageFile): String = withContext(Dispatchers.IO) {
        // Validar que el ID no tenga caracteres prohibidos (opcional pero recomendado)
        require(!imageFile.id.contains(Regex("""[\.\$#\[\]/]"""))) {
            "ID de imagen contiene caracteres inválidos: ${imageFile.id}"
        }

        // Guardar en ambas colecciones (imagesRef y filesRef)
        imagesRef.child(imageFile.id).setValue(imageFile.toFirebaseMap()).await()
        filesRef.child(imageFile.id).setValue(imageFile.toFirebaseMap()).await()

        // Retornar el ID seguro (ya generado en CameraModel)
        imageFile.id
    }

    suspend fun getAllImages(): List<ImageFile> = withContext(Dispatchers.IO) {
        filesRef.get().await()
            .children
            .mapNotNull { File.fromSnapshot(it) as? ImageFile }
    }

    fun observeAllImages(): Flow<List<ImageFile>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val images = snapshot.children.mapNotNull {
                    File.fromSnapshot(it) as? ImageFile
                }
                trySend(images)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        imagesRef.addValueEventListener(listener)
        awaitClose { imagesRef.removeEventListener(listener) }
    }

    suspend fun getImageById(id: String): ImageFile? = withContext(Dispatchers.IO) {
        (File.fromSnapshot(imagesRef.child(id).get().await()) as? ImageFile)
            ?: (File.fromSnapshot(filesRef.child(id).get().await()) as? ImageFile)
    }


    suspend fun getImagesByGroup(groupId: String): List<ImageFile> {
        return observeFilesByGroup(groupId)
            .first()
            .filterIsInstance<ImageFile>()
    }

    fun observeImagesByGroup(groupId: String): Flow<List<ImageFile>> {
        return observeFilesByGroup(groupId).map { files ->
            files.filterIsInstance<ImageFile>()
        }
    }

    suspend fun updateFile(file: File) {
        // Validación de ID seguro
        require(!file.id.contains(Regex("""[\.\$#\[\]/]"""))) {
            "ID inválido: ${file.id}"
        }

        filesRef.child(file.id).setValue(file.toFirebaseMap()).await()

        // Actualizar en colección específica si es necesario
        when (file) {
            is ImageFile -> imagesRef.child(file.id).setValue(file.toFirebaseMap()).await()
            is File.TextFile -> textFilesRef.child(file.id).setValue(file.toFirebaseMap()).await()
            is File.OcrResultFile -> textFilesRef.child(file.id).setValue(file.toFirebaseMap()).await()
        }
    }

    fun getProcessingState(userId: String): Flow<Boolean> = callbackFlow {
        val processingRef = db.getReference("processingStates/$userId") // Nueva estructura recomendada

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isProcessing = snapshot.getValue(Boolean::class.java) ?: false
                trySend(isProcessing) // Emite el estado actual
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        processingRef.addValueEventListener(listener)
        awaitClose { processingRef.removeEventListener(listener) }
    }


}