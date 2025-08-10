package com.cnunez.docufast.user.group.detail.contract

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.FileType

interface GroupDetailContract {
    interface View {
        /**
         * Muestra la lista de archivos en la interfaz
         * @param files Lista de archivos a mostrar
         * @param filterType Tipo de archivo a filtrar (opcional)
         */
        fun showFiles(files: List<File>, filterType: FileType? = FileType.TEXT)

        /**
         * Muestra un mensaje de error al usuario
         * @param message Mensaje de error a mostrar
         */
        fun showError(message: String)

        /**
         * Navega a la vista de detalle de un archivo
         * @param file Archivo seleccionado
         */
        fun showFileDetail(file: File)
    }

    interface Presenter {

        fun observeGroupFiles(groupId: String, organizationId: String, filterType: FileType?)
        /**
         * Carga los archivos de un grupo específico
         * @param groupId ID del grupo
         * @param organizationId ID de la organización
         * @param filterType Tipo de archivo a filtrar (opcional)
         */
        fun loadGroupFiles(groupId: String, organizationId: String, filterType: FileType? = FileType.TEXT)

        /**
         * Maneja la selección de un archivo
         * @param file Archivo seleccionado
         */
        fun onFileSelected(file: File)
    }

    interface Model {
        /**
         * Obtiene los archivos de un grupo desde la fuente de datos
         * @param groupId ID del grupo
         * @param organizationId ID de la organización
         * @param filterType Tipo de archivo a filtrar (opcional)
         * @param callback Callback con la lista de archivos o mensaje de error
         */
        @Deprecated("Usar observeGroupFiles para actualizaciones en tiempo real")
        fun fetchGroupFiles(
            groupId: String,
            organizationId: String,
            filterType: FileType? = FileType.TEXT,
            callback: (List<File>?, String?) -> Unit
        )

        fun observeGroupFiles(
            groupId: String,
            organizationId: String,
            filterType: FileType?,
            callback: (List<File>?, String?) -> Unit
        )

        /**
         * Obtiene la URL de descarga de un archivo
         * @param file Archivo del cual obtener la URL
         * @param callback Callback con la URL o mensaje de error
         */
        fun getFileDownloadUrl(
            file: File,
            callback: (String?, String?) -> Unit
        )
    }
}