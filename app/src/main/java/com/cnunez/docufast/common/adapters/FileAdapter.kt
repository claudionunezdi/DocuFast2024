package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.File

class FileAdapter(
    private var files: List<File>,
    private val onOpenClick: (File) -> Unit,
    private val onEditClick: (File) -> Unit = {}, // Hacer opcionales
    private val onViewContentClick: (File) -> Unit = {},
    private val showCreationDate: Boolean = false // Nuevo parámetro
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val fileName: TextView = view.findViewById(R.id.textViewFileName)
        private val creationDate: TextView? = if (showCreationDate) view.findViewById(R.id.textViewCreationDate) else null
        private val buttonOpen: Button? = view.findViewById(R.id.buttonOpen)
        private val buttonEdit: Button? = view.findViewById(R.id.buttonEdit)
        private val buttonContent: Button? = view.findViewById(R.id.buttonContent)

        fun bind(file: File) {
            fileName.text = file.name

            // Mostrar fecha si está configurado
            creationDate?.text = file.metadata.creationDate

            // Configurar botones (si existen en el layout)
            buttonOpen?.setOnClickListener { onOpenClick(file) }
            buttonEdit?.setOnClickListener { onEditClick(file) }
            buttonContent?.setOnClickListener { onViewContentClick(file) }

            // Click general en el item si no hay botones
            if (buttonOpen == null && buttonEdit == null && buttonContent == null) {
                itemView.setOnClickListener { onOpenClick(file) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Elegir layout basado en configuración
        val layoutId = if (showCreationDate) R.layout.item_file_user else R.layout.item_file
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    fun setFiles(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }
}