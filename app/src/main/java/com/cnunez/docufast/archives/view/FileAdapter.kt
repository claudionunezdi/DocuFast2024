package com.cnunez.docufast.archives.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.File
import android.widget.TextView
import android.widget.Button

class FileAdapter(
    private var files: List<File>,
    private val onOpenClick: (File) -> Unit,
    private val onEditClick: (File) -> Unit,
    private val onViewContentClick: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val fileName: TextView = view.findViewById(R.id.textViewFileName)
        private val buttonOpen: Button = view.findViewById(R.id.buttonOpen)
        private val buttonEdit: Button = view.findViewById(R.id.buttonEdit)
        private val buttonContent: Button = view.findViewById(R.id.buttonContent)

        fun bind(file: File) {
            fileName.text = file.name
            buttonOpen.setOnClickListener { onOpenClick(file) }
            buttonEdit.setOnClickListener { onEditClick(file) }
            buttonContent.setOnClickListener { onViewContentClick(file) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    /** Actualiza la lista de archivos en el adapter */
    fun setFiles(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }
}
