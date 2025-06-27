package com.cnunez.docufast.archives.contract

import com.cnunez.docufast.common.dataclass.File
import com.google.firebase.auth.FirebaseUser

interface ArchivesContract {
    interface View {
        fun onUserAuthenticated(user: FirebaseUser)
        fun showFiles(files: List<File>)
        fun showError(message: String)
    }

    interface Presenter {
        /** Inicia la vista: autentica y carga archivos */
        fun start()
        /** Abrir la imagen externa */
        fun openFile(file: File)
        /** Editar en la app el contenido de texto */
        fun editFile(file: File)
        /** Mostrar el contenido de texto en pantalla */
        fun viewFileContent(file: File)
    }

    interface Model {
        /** Recupera la lista de archivos para el grupo dado */
        suspend fun fetchFiles(groupId: String): Result<List<File>>
    }
}
