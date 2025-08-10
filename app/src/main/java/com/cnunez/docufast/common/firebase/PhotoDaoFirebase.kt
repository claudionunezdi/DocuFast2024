// PhotoDaoFirebase.kt
package com.cnunez.docufast.common.firebase

//@file:Suppress("unused")

/*

import com.cnunez.docufast.common.dataclass.File.ImageFile
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

class PhotoDaoFirebase(private val db: FirebaseDatabase) {
    private val ref = db.getReference("imageFiles")

    /** Inserta ImageFile y devuelve su clave */
    suspend fun insert(photo: ImageFile): String = withContext(Dispatchers.IO) {
        val key = ref.push().key
            ?: throw Exception("No se pudo generar clave para ImageFile")
        photo.id = key
        ref.child(key).setValue(photo.toMap()).await()
        key
    }

    /** Obtiene todas las ImageFile (una sola vez) */
    suspend fun getAll(): List<ImageFile> = withContext(Dispatchers.IO) {
        val snap = ref.get().await()
        snap.children.mapNotNull { ImageFile.fromSnapshot(it) }
    }

    /** Observa cambios en imageFiles/ y emite la lista */
    fun observeAll(): Flow<List<ImageFile>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { ImageFile.fromSnapshot(it) }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Obtiene una sola ImageFile por su id */
    suspend fun getById(id: String): ImageFile? = withContext(Dispatchers.IO) {
        val snap = ref.child(id).get().await()
        if (snap.exists()) ImageFile.fromSnapshot(snap) else null
    }
}
*/