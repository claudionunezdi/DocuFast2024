package com.cnunez.docufast.common.firebase
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * DAO para manejar operaciones CRUD de User en Firebase Realtime Database.
 */
class UserDaoRealtime(private val db: FirebaseDatabase) {
    private val ref = db.getReference("users")

    /**
     * Inserta un nuevo usuario y devuelve su id generado.
     */
    suspend fun insert(user: User): String = withContext(Dispatchers.IO) {
        val key = ref.push().key
            ?: throw Exception("No se pudo generar clave para User")
        user.id = key
        ref.child(key)
            .setValue(user.toMap())
            .await()
        key
    }

    /**
     * Actualiza un usuario existente (requiere user.id). No devuelve valor.
     */
    suspend fun update(user: User) = withContext(Dispatchers.IO) {
        if (user.id.isBlank()) throw IllegalArgumentException("User.id no puede estar vacío al actualizar")
        ref.child(user.id)
            .setValue(user.toMap())
            .await()
    }

    /**
     * Elimina un usuario por su id.
     */
    suspend fun delete(userId: String) = withContext(Dispatchers.IO) {
        ref.child(userId)
            .removeValue()
            .await()
    }

    /**
     * Obtiene la lista completa de usuarios.
     */
    suspend fun getAll(): List<User> = withContext(Dispatchers.IO) {
        val snapshot = ref.get().await()
        snapshot.children.mapNotNull { it.toUser() }
    }

    /**
     * Obtiene un usuario por su id, o null si no existe.
     */
    suspend fun getById(userId: String): User? = withContext(Dispatchers.IO) {
        val snap = ref.child(userId).get().await()
        if (snap.exists()) snap.toUser() else null
    }

    /**
     * Mapea un DataSnapshot a User.
     */
    private fun DataSnapshot.toUser(): User? {
        val id = key ?: return null
        val name = child("name").getValue(String::class.java).orEmpty()
        val email = child("email").getValue(String::class.java).orEmpty()
        val organization = child("organization").getValue(String::class.java).orEmpty()
        val workGroupsType = object : GenericTypeIndicator<Map<String, Boolean>>() {}
        val workGroups = child("workGroups").getValue(workGroupsType) ?: emptyMap()
        val role = child("role").getValue(String::class.java).orEmpty()
        val stability = child("stability").getValue(Int::class.java) ?: 0
        val createdAt = child("createdAt").getValue(Long::class.java) ?: 0L
        return User(
            id = id,
            name = name,
            email = email,
            organization = organization,
            workGroups = workGroups,
            role = role,
            stability = stability,
            createdAt = createdAt
        )
    }
}
