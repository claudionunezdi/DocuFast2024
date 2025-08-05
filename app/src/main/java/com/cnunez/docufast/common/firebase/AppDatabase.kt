package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.database.FirebaseDatabase

object AppDatabase {
    // Instancia perezosa de FirebaseDatabase, sin setPersistenceEnabled aquí
    private val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    // Opcional: si quieres que cada DAO mantenga sincronizada su rama, podrías
    // llamar aquí a keepSynced(true) sobre sus referencias en el init{} de cada uno.
    // Pero si ya lo haces en MyApp, no es necesario repetirlo.

    // DAOs para cada tipo de recurso
    val photoDao: PhotoDaoFirebase by lazy { PhotoDaoFirebase(firebaseDatabase) }
    val textFileDao: TextFileDaoFirebase by lazy { TextFileDaoFirebase(firebaseDatabase) }
    val fileDao: FileDaoRealtime by lazy { FileDaoRealtime(firebaseDatabase) }
    val groupDao: GroupDaoRealtime by lazy { GroupDaoRealtime(firebaseDatabase) }
    suspend fun getCurrentUserFiles(): List<File> {
        return fileDao.getFilesForCurrentUser()
    }

    suspend fun getCurrentUserGroups(): List<Group> {
        return groupDao.getGroupsForCurrentUser()
    }



    suspend fun getOrganizationGroups(organizationId: String): List<Group> {
        return groupDao.getGroupsByOrganization(organizationId)
    }

}