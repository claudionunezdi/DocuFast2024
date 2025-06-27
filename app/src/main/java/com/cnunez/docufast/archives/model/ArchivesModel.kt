package com.cnunez.docufast.archives.model

import com.cnunez.docufast.archives.contract.ArchivesContract
import com.cnunez.docufast.common.dataclass.File
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ArchivesModel(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ArchivesContract.Model {

    private val filesRef = db.getReference("files")

    override suspend fun fetchFiles(groupId: String): Result<List<File>> = runCatching {
        val snapshot = filesRef.orderByChild("groupId")
            .equalTo(groupId)
            .get()
            .await()
        snapshot.children.mapNotNull { child ->
            child.getValue(File::class.java)?.apply { id = child.key.orEmpty() }
        }
    }
}
