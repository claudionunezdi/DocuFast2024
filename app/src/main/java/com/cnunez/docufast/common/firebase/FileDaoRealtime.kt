package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.File
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FileDaoRealtime(private val db: FirebaseDatabase) {
    private val ref = db.getReference("files")

    /** Inserta File completo y devuelve su clave */
    suspend fun insert(file: File): String = withContext(Dispatchers.IO) {
        val key = ref.push().key
            ?: throw Exception("No se pudo generar clave para File")
        file.id = key
        ref.child(key).setValue(file.toMap()).await()
        key
    }

    /** Obtiene todos los File una sola vez */

    suspend fun getAll(): List<File>{
        val snapshot = ref.get().await()
        return snapshot.children.mapNotNull { File.fromSnapshot(it) }
    }

    /** Obtiene un File por su id */
    suspend fun getById(id: String): File? = withContext(Dispatchers.IO) {
        val snap = ref.child(id).get().await()
        if (snap.exists()) File.fromSnapshot(snap) else null
    }




}
