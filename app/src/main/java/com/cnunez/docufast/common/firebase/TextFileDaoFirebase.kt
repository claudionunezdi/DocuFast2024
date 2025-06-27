// TextFileDaoFirebase.kt
package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.TextFile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TextFileDaoFirebase(private val db: FirebaseDatabase) {
    private val ref = db.getReference("textFiles")

    /** Inserta TextFile y devuelve su clave */
    suspend fun insert(textFile: TextFile): String = withContext(Dispatchers.IO) {
        val key = ref.push().key
            ?: throw Exception("No se pudo generar clave para TextFile")
        textFile.id = key
        ref.child(key).setValue(textFile.toMap()).await()
        key
    }

    /** Actualiza un TextFile existente */
    suspend fun update(textFile: TextFile) = withContext(Dispatchers.IO) {
        ref.child(textFile.id).setValue(textFile.toMap()).await()
    }

    /** Obtiene todos los TextFile (una sola vez) */
    suspend fun getAll(): List<TextFile> = withContext(Dispatchers.IO) {
        val snap = ref.get().await()
        snap.children.mapNotNull { TextFile.fromSnapshot(it) }
    }

    /** Observa cambios en textFiles/ y emite la lista */
    fun observeAll(): Flow<List<TextFile>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { TextFile.fromSnapshot(it) }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Obtiene un solo TextFile por su id */
    suspend fun getTextFileById(id: String): TextFile? = withContext(Dispatchers.IO) {
        val snap = ref.child(id).get().await()
        if (snap.exists()) TextFile.fromSnapshot(snap) else null
    }
}
