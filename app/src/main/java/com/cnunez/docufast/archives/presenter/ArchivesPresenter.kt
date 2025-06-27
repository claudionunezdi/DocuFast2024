package com.cnunez.docufast.archives.presenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.cnunez.docufast.archives.contract.ArchivesContract
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.fileContent.view.FileContentActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArchivesPresenter(
    private val view: ArchivesContract.View,
    private val model: ArchivesContract.Model,
    private val context: Context
) : ArchivesContract.Presenter {

    private val auth = FirebaseAuth.getInstance()

    override fun start() {
        auth.currentUser?.let { user ->
            view.onUserAuthenticated(user)
            CoroutineScope(Dispatchers.Main).launch {
                val groupId = user.uid // o según tu lógica
                model.fetchFiles(groupId)
                    .onSuccess { files -> view.showFiles(files) }
                    .onFailure { err -> view.showError(err.message ?: "Error al cargar archivos") }
            }
        } ?: view.showError("Usuario no autenticado")
    }

    override fun openFile(file: File) {
        // Intent para ver imagen
        val uri = Uri.parse(file.imageFile.uri)
        Intent(Intent.ACTION_VIEW).apply {
            data = uri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also { context.startActivity(it) }
    }

    override fun editFile(file: File) {
        // En esta versión, lanzamos la misma vista de contenido para editar
        viewFileContent(file)
    }

    override fun viewFileContent(file: File) {
        Intent(context, FileContentActivity::class.java).apply {
            putExtra("FILE_ID", file.id)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }.also { context.startActivity(it) }
    }
}
