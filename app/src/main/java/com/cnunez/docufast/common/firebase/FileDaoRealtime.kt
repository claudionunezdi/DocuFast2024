package com.cnunez.docufast.common.firebase

import android.net.Uri
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.File.ImageFile
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
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
    // ÚNICA verdad para archivos según el seed: /files/{fileId}
    private val filesRef = db.getReference("files")

    // Colecciones de compatibilidad (si aún existen en tu RTDB)
    private val imagesRef = db.getReference("images")
    private val textFilesRef = db.getReference("textFiles")

    // -------------------- Subidas / Creación --------------------

    /**
     * Sube el archivo al Storage y lo persiste en /files.
     * - Si file.storageInfo.path viene vacío, se calcula un path “bonito”.
     * - También registra el fileId bajo /groups/{groupId}/files/{fileId}: true (mapa de refs).
     */
    suspend fun uploadFileWithUri(file: File, localUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val fileId = if (file.id.isNotBlank()) file.id else filesRef.push().key
                ?: throw Exception("Error generando ID")

            // Path por defecto si no viene:
            val computedPath = when (file) {
                is File.ImageFile -> {
                    // Mantiene tu convención de seed (carpeta por org/grupo/imagen)
                    val org = file.metadata.organizationId.ifBlank { "default_org" }
                    val gid = file.metadata.groupId.ifBlank { "default_group" }
                    val name = file.name.ifBlank { "$fileId.jpg" }
                    "organizations/$org/groups/$gid/images/$fileId/$name"
                }
                is File.TextFile -> {
                    // Textos OCR por imagen
                    val org = file.metadata.organizationId.ifBlank { "default_org" }
                    val src = file.sourceImageId ?: fileId
                    "orgs/$org/ocr/$src/$fileId.txt"
                }
                else -> throw IllegalArgumentException("Tipo de archivo no soportado")
            }

            val storagePath = file.storageInfo.path.ifBlank { computedPath }
            val downloadUrl = storageManager.uploadFile(localUri, storagePath).toString()

            val fileToUpload = when (file) {
                is File.ImageFile -> file.copy(
                    id = fileId,
                    storageInfo = file.storageInfo.copy(path = storagePath, downloadUrl = downloadUrl)
                )
                is File.TextFile -> file.copy(
                    id = fileId,
                    storageInfo = file.storageInfo.copy(path = storagePath, downloadUrl = downloadUrl)
                )
                else -> file
            }

            filesRef.child(fileId).setValue(fileToUpload.toFirebaseMap()).await()
            // Mantenemos el mapa de referencias en grupos:
            if (fileToUpload.metadata.groupId.isNotBlank()) {
                db.getReference("groups/${fileToUpload.metadata.groupId}/files/$fileId")
                    .setValue(true).await()
            }

            // Compatibilidad opcional:
            when (fileToUpload) {
                is ImageFile -> imagesRef.child(fileId).setValue(fileToUpload.toFirebaseMap()).await()
                is File.TextFile -> textFilesRef.child(fileId).setValue(fileToUpload.toFirebaseMap()).await()
                is File.OcrResultFile -> TODO()
            }

            fileId
        } catch (e: Exception) {
            throw Exception("Error subiendo archivo: ${e.message}")
        }
    }

    /**
     * Crea y guarda un TextFile (resultado OCR) asociado a una ImageFile.
     * Solo escribe en /files (y en textFilesRef por compatibilidad).
     */
    suspend fun saveOcrResult(
        imageFile: File.ImageFile,
        extractedText: String,
        confidence: Float,
        userId: String
    ): File.TextFile = withContext(Dispatchers.IO) {
        val textId = filesRef.push().key ?: throw Exception("Error generando ID")
        val org = imageFile.metadata.organizationId
        val storagePath = "orgs/$org/ocr/${imageFile.id}/$textId.txt"

        val textFile = File.TextFile(
            id = textId,
            name = "ocr_${imageFile.name}",
            metadata = File.FileMetadata(
                createdBy = userId,
                groupId = imageFile.metadata.groupId,
                organizationId = org
            ),
            storageInfo = File.StorageInfo(path = storagePath, downloadUrl = ""),
            content = extractedText,
            sourceImageId = imageFile.id,
            ocrData = File.TextFile.OcrMetadata(
                confidence = confidence,
                processingTimeMs = System.currentTimeMillis()
            ),
            language = "es"
        )

        filesRef.child(textId).setValue(textFile.toFirebaseMap()).await()
        textFilesRef.child(textId).setValue(textFile.toFirebaseMap()).await() // compatibilidad
        // Vincula ambos
        filesRef.child(imageFile.id).child("linkedOcrTextId").setValue(textId).await()

        textFile
    }

    // -------------------- Lecturas / Observables --------------------

    suspend fun getFileById(id: String): File? = withContext(Dispatchers.IO) {
        filesRef.child(id).get().await().let { snapshot -> File.fromSnapshot(snapshot) }
    }

    fun observeFilesByGroup(groupId: String): Flow<List<File>> = callbackFlow {
        // Necesita indexOn: "metadata/groupId"
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

    suspend fun getFilesByGroup(groupId: String): List<File> {
        return observeFilesByGroup(groupId).first()
    }

    suspend fun getFilesByType(groupId: String, type: FileType): List<File> {
        return observeFilesByGroup(groupId).first().filter { it.type == type }
    }

    suspend fun getFilesByUserId(userId: String): List<File> = withContext(Dispatchers.IO) {
        // Necesita indexOn: "metadata/createdBy"
        val snapshot = filesRef.orderByChild("metadata/createdBy").equalTo(userId).get().await()
        snapshot.children.mapNotNull { File.fromSnapshot(it) }
    }

    // -------------------- Actualizaciones de texto + Storage --------------------

    /**
     * Actualiza el contenido de un TextFile *por id* y sincroniza Storage si es necesario.
     */
    suspend fun updateTextFileContentAndStorage(
        textFileId: String,
        newContent: String,
        organizationId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val snap = filesRef.child(textFileId).get().await()
            val textFile = File.fromSnapshot(snap) as? File.TextFile
                ?: return@withContext Result.failure(IllegalArgumentException("No es TextFile"))

            val path = textFile.storageInfo.path.ifBlank { "orgs/$organizationId/texts/$textFileId.txt" }
            val url = storageManager.uploadBytes(newContent.toByteArray(Charsets.UTF_8), path).toString()

            val updates = mapOf(
                "content" to newContent,
                "storageInfo/path" to path,
                "storageInfo/downloadUrl" to url
            )
            filesRef.child(textFileId).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca el TextFile por sourceImageId y actualiza su contenido + Storage.
     * Si en vez de TextFile solo existe OCR_RESULT, actualiza extractedText y opcionalmente guarda un .txt.
     */
    suspend fun updateTextBySourceImageIdAndStorage(
        imageId: String,
        newContent: String,
        organizationId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1) TEXT por sourceImageId (indexOn: "sourceImageId")
            val textSnap = filesRef.orderByChild("sourceImageId").equalTo(imageId).limitToFirst(1).get().await()
            if (textSnap.hasChildren()) {
                val node = textSnap.children.first()
                val textFileId = node.key ?: return@withContext Result.failure(Exception("ID inválido"))

                val path = node.child("storageInfo/path").getValue(String::class.java).orEmpty()
                    .ifBlank { "orgs/$organizationId/ocr/$imageId/$textFileId.txt" }
                val url = storageManager.uploadBytes(newContent.toByteArray(Charsets.UTF_8), path).toString()

                val updates = mapOf(
                    "content" to newContent,
                    "storageInfo/path" to path,
                    "storageInfo/downloadUrl" to url
                )
                node.ref.updateChildren(updates).await()
                return@withContext Result.success(Unit)
            }

            // 2) OCR_RESULT por originalImage/imageId (indexOn: "originalImage/imageId")
            val ocrSnap = filesRef.orderByChild("originalImage/imageId").equalTo(imageId).limitToFirst(1).get().await()
            if (ocrSnap.hasChildren()) {
                val node = ocrSnap.children.first()
                // Guardar extractedText (y opcionalmente un txt en Storage)
                node.ref.child("extractedText").setValue(newContent).await()

                val path = node.child("storageInfo/path").getValue(String::class.java).orEmpty()
                    .ifBlank { "orgs/$organizationId/ocr/$imageId/extracted.txt" }
                val url = storageManager.uploadBytes(newContent.toByteArray(Charsets.UTF_8), path).toString()

                val updates = mapOf(
                    "storageInfo/path" to path,
                    "storageInfo/downloadUrl" to url
                )
                node.ref.updateChildren(updates).await()
                return@withContext Result.success(Unit)
            }

            // 3) IMAGE -> linkedOcrTextId
            val imgNode = filesRef.child(imageId).get().await()
            if (imgNode.exists()) {
                val linkedTextId = imgNode.child("linkedOcrTextId").getValue(String::class.java)
                if (!linkedTextId.isNullOrBlank()) {
                    val textNode = filesRef.child(linkedTextId).get().await()
                    if (textNode.exists()) {
                        val path = textNode.child("storageInfo/path").getValue(String::class.java).orEmpty()
                            .ifBlank { "orgs/$organizationId/ocr/$imageId/$linkedTextId.txt" }
                        val url = storageManager.uploadBytes(newContent.toByteArray(Charsets.UTF_8), path).toString()

                        val updates = mapOf(
                            "content" to newContent,
                            "storageInfo/path" to path,
                            "storageInfo/downloadUrl" to url
                        )
                        textNode.ref.updateChildren(updates).await()
                        return@withContext Result.success(Unit)
                    }
                }
            }

            Result.failure(IllegalStateException("No se encontró texto relacionado a la imagen"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * API simple para el Presenter: detecta qué nodo tocar (TEXT/OCR_RESULT/linked) y actualiza.
     * ¡Siempre trabaja contra /files, no contra /organizations/.../groups/.../files!
     */
    suspend fun saveOcrTextSmart(
        organizationId: String,
        imageId: String,
        newContent: String,
        groupId: String? = null, // solo informativo; no se usa para buscar
        alsoUploadToStorage: Boolean = true
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1) TEXT por sourceImageId
            val textSnap = filesRef.orderByChild("sourceImageId")
                .equalTo(imageId).limitToFirst(1).get().await()
            if (textSnap.hasChildren()) {
                val node = textSnap.children.first()
                node.ref.child("content").setValue(newContent).await()
                if (alsoUploadToStorage) {
                    updateStorageInfoForNode(node, newContent, organizationId, isOcrResult = false, imageId = imageId)
                }
                return@withContext Result.success(Unit)
            }

            // 2) OCR_RESULT por originalImage/imageId
            val ocrSnap = filesRef.orderByChild("originalImage/imageId")
                .equalTo(imageId).limitToFirst(1).get().await()
            if (ocrSnap.hasChildren()) {
                val node = ocrSnap.children.first()
                node.ref.child("extractedText").setValue(newContent).await()
                if (alsoUploadToStorage) {
                    updateStorageInfoForNode(node, newContent, organizationId, isOcrResult = true, imageId = imageId)
                }
                return@withContext Result.success(Unit)
            }

            // 3) IMAGE con linkedOcrTextId
            val imageNode = filesRef.child(imageId).get().await()
            if (imageNode.exists()) {
                val linkedTextId = imageNode.child("linkedOcrTextId").getValue(String::class.java)
                if (!linkedTextId.isNullOrBlank()) {
                    val textNode = filesRef.child(linkedTextId).get().await()
                    if (textNode.exists()) {
                        textNode.ref.child("content").setValue(newContent).await()
                        if (alsoUploadToStorage) {
                            updateStorageInfoForNode(textNode, newContent, organizationId, isOcrResult = false, imageId = imageId)
                        }
                        return@withContext Result.success(Unit)
                    }
                }
            }

            Result.failure(IllegalStateException("No se encontró texto relacionado a la imagen"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------- Descargas / Eliminado --------------------

    suspend fun getFileDownloadUrl(fileId: String): String? = withContext(Dispatchers.IO) {
        val f = getFileById(fileId) ?: return@withContext null
        if (f.storageInfo.downloadUrl.isNotBlank()) return@withContext f.storageInfo.downloadUrl

        // Genera downloadUrl desde path si está vacío
        if (f.storageInfo.path.isBlank()) return@withContext null
        val url = storageManager.getDownloadUrl(f.storageInfo.path)
        filesRef.child(fileId).child("storageInfo/downloadUrl").setValue(url).await()
        url
    }

    suspend fun deleteFile(fileId: String, groupId: String) = withContext(Dispatchers.IO) {
        try {
            getFileById(fileId)?.let { file ->
                // 1) Storage
                if (file.storageInfo.path.isNotBlank()) {
                    storageManager.deleteFile(file.storageInfo.path)
                }
                // 2) /files
                filesRef.child(fileId).removeValue().await()
                // 3) mapas de compatibilidad
                imagesRef.child(fileId).removeValue().await()
                textFilesRef.child(fileId).removeValue().await()
                // 4) referencia en grupo (mapa)
                db.getReference("groups/$groupId/files/$fileId").removeValue().await()
            }
        } catch (e: Exception) {
            throw Exception("Error eliminando archivo: ${e.message}")
        }
    }

    // -------------------- Imágenes helpers (compat) --------------------

    suspend fun insertImage(imageFile: File.ImageFile): String = withContext(Dispatchers.IO) {
        require(!imageFile.id.contains(Regex("""[\.\$#\[\]/]"""))) {
            "ID de imagen contiene caracteres inválidos: ${imageFile.id}"
        }
        filesRef.child(imageFile.id).setValue(imageFile.toFirebaseMap()).await()
        imagesRef.child(imageFile.id).setValue(imageFile.toFirebaseMap()).await()
        imageFile.id
    }

    suspend fun getAllImages(): List<ImageFile> = withContext(Dispatchers.IO) {
        filesRef.get().await().children.mapNotNull { File.fromSnapshot(it) as? ImageFile }
    }

    fun observeAllImages(): Flow<List<ImageFile>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val images = snapshot.children.mapNotNull { File.fromSnapshot(it) as? ImageFile }
                trySend(images)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        imagesRef.addValueEventListener(listener)
        awaitClose { imagesRef.removeEventListener(listener) }
    }

    suspend fun getImageById(id: String): ImageFile? = withContext(Dispatchers.IO) {
        (File.fromSnapshot(imagesRef.child(id).get().await()) as? ImageFile)
            ?: (File.fromSnapshot(filesRef.child(id).get().await()) as? ImageFile)
    }

    suspend fun getImagesByGroup(groupId: String): List<ImageFile> {
        return observeFilesByGroup(groupId).first().filterIsInstance<ImageFile>()
    }

    fun observeImagesByGroup(groupId: String): Flow<List<ImageFile>> {
        return observeFilesByGroup(groupId).map { files -> files.filterIsInstance<ImageFile>() }
    }

    suspend fun updateFile(file: File) {
        require(!file.id.contains(Regex("""[\.\$#\[\]/]"""))) { "ID inválido: ${file.id}" }
        filesRef.child(file.id).setValue(file.toFirebaseMap()).await()
        // Compat opcional
        when (file) {
            is ImageFile -> imagesRef.child(file.id).setValue(file.toFirebaseMap()).await()
            is File.TextFile -> textFilesRef.child(file.id).setValue(file.toFirebaseMap()).await()
            is File.OcrResultFile -> {} // no espejamos OCR_RESULT en textFilesRef
        }
    }

    // -------------------- Helpers privados --------------------

    private suspend fun updateStorageInfoForNode(
        node: DataSnapshot,
        content: String,
        orgId: String,
        isOcrResult: Boolean,
        imageId: String
    ) {
        val fileId = node.key ?: return
        val defaultPath = if (isOcrResult)
            "orgs/$orgId/ocr/$imageId/extracted.txt"
        else
            "orgs/$orgId/ocr/$imageId/$fileId.txt"

        val storagePath = node.child("storageInfo/path").getValue(String::class.java).orEmpty()
            .ifBlank { defaultPath }

        val ref = FirebaseStorage.getInstance().getReference(storagePath)
        ref.putBytes(content.toByteArray(Charsets.UTF_8)).await()
        val url = ref.downloadUrl.await().toString()

        val updates = mapOf(
            "storageInfo/path" to storagePath,
            "storageInfo/downloadUrl" to url
        )
        node.ref.updateChildren(updates).await()
    }
}
