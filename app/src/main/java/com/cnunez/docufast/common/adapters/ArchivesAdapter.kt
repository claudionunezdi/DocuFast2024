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

class ArchivesAdapter(
    private val files: MutableList<File> = mutableListOf(), // Cambiado a MutableList
    private val onItemClick: ((File) -> Unit)? = null
) : RecyclerView.Adapter<ArchivesAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int = files.size

    fun setFiles(newFiles: List<File>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged()
    }


    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.textViewFileName)
        private val dateTv: TextView = itemView.findViewById(R.id.textViewFileCreationDate)
        private val photoIv: ImageView = itemView.findViewById(R.id.imageViewFilePhoto)
        private val textTv: TextView = itemView.findViewById(R.id.textViewExtractedText)

        fun bind(file: File) {
            nameTv.text = file.name
            dateTv.text = file.creationDate
            // Foto: usamos imageFile.uri
            Glide.with(itemView.context)
                .load(file.imageFile.uri)
                .into(photoIv)
            // Texto extraído
            textTv.text = file.textFile.content

            itemView.setOnClickListener { onItemClick?.invoke(file) }
        }
    }
}
