package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.cnunez.docufast.common.firebase.storage.FirebaseStorageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppDatabase {
    // Configuración centralizada de Firebase
    val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Inicialización perezosa de DAOs
    val fileDao: FileDaoRealtime by lazy {
        FileDaoRealtime(
            db = firebaseDatabase,
            storageManager = FileStorageManager() // Crea instancia explícita
        )
    }

    val groupDao: GroupDaoRealtime by lazy {
        GroupDaoRealtime(firebaseDatabase)
    }

    // Obtiene archivos del usuario actual de forma segura
    suspend fun getCurrentUserFiles(): List<File> = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")

        fileDao.getFilesByUserId(userId)
    }

    // Versión alternativa con manejo de errores
    suspend fun getCurrentUserFilesSafe(): Result<List<File>> = try {
        Result.success(getCurrentUserFiles())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Métodos para grupos (ejemplo)
    suspend fun getCurrentUserGroups(): List<Group> = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@withContext emptyList()

        groupDao.getGroupsForCurrentUser(userId)
    }

    suspend fun getOrganizationGroups(organizationId: String): List<Group> {
        return groupDao.getGroupsByOrganization(organizationId)
    }
}