package com.cnunez.docufast.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.File.TextFile

class FileAdapter(private val listener: (TextFile) -> Unit) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var files: List<TextFile> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_user, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    fun setFiles(files: List<TextFile>) {
        this.files = files
        notifyDataSetChanged()
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)
        private val textViewCreationDate: TextView = itemView.findViewById(R.id.textViewCreationDate)

        fun bind(file: TextFile) {
            textViewFileName.text = file.name

            textViewCreationDate.text = file.metadata.toString()
            itemView.setOnClickListener {
                listener(file)
            }
        }
    }
}