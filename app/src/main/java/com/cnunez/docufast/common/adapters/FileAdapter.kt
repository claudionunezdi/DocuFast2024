package com.cnunez.docufast.common.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.FileType
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private var files: List<File> = emptyList(),
    private val onOpenClick: (File) -> Unit,
    private val onEditClick: (File) -> Unit = {},
    private val onViewContentClick: (File) -> Unit = {},
    private val showCreatorInfo: Boolean = true,
    private val showCreationDate: Boolean = false
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    // ViewHolder que maneja todos los elementos de la UI
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileName: TextView = itemView.findViewById(R.id.textViewFileName)
        private val fileCreator: TextView = itemView.findViewById(R.id.textViewFileCreator)
        private val fileDate: TextView = itemView.findViewById(R.id.textViewFileDate)
        private val fileIcon: ImageView = itemView.findViewById(R.id.imageViewFileIcon)
        private val buttonOpen: Button? = itemView.findViewById(R.id.buttonOpen)
        private val buttonEdit: Button? = itemView.findViewById(R.id.buttonEdit)
        private val buttonContent: Button? = itemView.findViewById(R.id.buttonContent)


        @SuppressLint("SetTextI18n")
        fun bind(file: File) {
            // Configurar elementos básicos
            fileName.text = file.name

            // Mostrar información del creador si está configurado
            if (showCreatorInfo) {
                fileCreator.text = "Subido por: ${file.metadata.creatorName
                    .takeIf { it.isNotEmpty() }
                    ?: "Usuario ${file.metadata.createdBy.take(6)}"}"
                fileCreator.visibility = View.VISIBLE
            } else {
                fileCreator.visibility = View.GONE
            }

            // Formatear y mostrar fecha
            fileDate.text = formatDate(file.metadata.creationDate)

            // Configurar icono según tipo de archivo
            fileIcon.setImageResource(when(file.type) {
                FileType.IMAGE -> R.drawable.ic_image
                FileType.TEXT -> R.drawable.ic_text
                FileType.PDF -> R.drawable.ic_pdf
                else -> R.drawable.ic_file
            })

            // Configurar botones si existen
            buttonOpen?.setOnClickListener { onOpenClick(file) }
            buttonEdit?.setOnClickListener { onEditClick(file) }
            buttonContent?.setOnClickListener { onViewContentClick(file) }

            // Si no hay botones, hacer clickable el item completo
            if (buttonOpen == null && buttonEdit == null && buttonContent == null) {
                itemView.setOnClickListener { onOpenClick(file) }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd MMM yyyy 'a las' HH:mm", Locale.getDefault())
                val date = parser.parse(dateString)
                formatter.format(date ?: Date())
            } catch (e: Exception) {
                "Fecha desconocida"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_with_metadata, parent, false)
        return FileViewHolder(view)
    }


    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }
}