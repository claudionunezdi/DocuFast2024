package com.cnunez.docufast.archives.View

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.archives.Contract.ArchivesContract
import java.io.File
import android.widget.TextView
import android.widget.Button

class FileAdapter(private val files: List<File>, private val onFileClick: (File) -> Unit) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    class ViewHolder(view: View, private val onFileClick: (File) -> Unit) : RecyclerView.ViewHolder(view) {
        private val fileName: TextView = view.findViewById(R.id.FileName)
        private val viewButton: Button = view.findViewById(R.id.ViewButton)

        fun bind(file: File) {
            fileName.text = file.name
            viewButton.setOnClickListener { onFileClick(file) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view, onFileClick)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }
}

