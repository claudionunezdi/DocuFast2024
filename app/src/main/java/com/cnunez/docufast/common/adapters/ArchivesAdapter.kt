package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.File

class ArchivesAdapter(private var files: List<File>) : RecyclerView.Adapter<ArchivesAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int = files.size

    fun setFiles(newFiles: MutableList<java.io.File>) {
        val oldSize = files.size
        files = newFiles.map { file ->
            com.cnunez.docufast.common.dataclass.File(
                name = file.name,
                creationDate = "", // Set appropriate value
                photoUrl = "", // Set appropriate value
                extractedText = "" // Set appropriate value
            )
        }.toMutableList()
        val newSize = files.size

        when {
            oldSize < newSize -> notifyItemRangeInserted(oldSize, newSize - oldSize)
            oldSize > newSize -> notifyItemRangeRemoved(newSize, oldSize - newSize)
            else -> notifyDataSetChanged()
        }
    }
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)
        private val textViewFileCreationDate: TextView = itemView.findViewById(R.id.textViewFileCreationDate)
        private val imageViewFilePhoto: ImageView = itemView.findViewById(R.id.imageViewFilePhoto)
        private val textViewExtractedText: TextView = itemView.findViewById(R.id.textViewExtractedText)

        fun bind(file: File) {
            textViewFileName.text = file.name
            textViewFileCreationDate.text = file.creationDate
            Glide.with(itemView.context).load(file.photoUrl).into(imageViewFilePhoto)
            textViewExtractedText.text = file.extractedText
        }
    }
}