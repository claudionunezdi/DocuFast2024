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
import com.cnunez.docufast.common.dataclass.FileType
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import java.text.SimpleDateFormat
import java.util.*

class ArchivesAdapter(
    private val fileDao: FileDaoRealtime,
    private var files: List<File> = emptyList(),
    private val onItemClick: (File) -> Unit = {},
    private val onOcrResultClick: (File.TextFile) -> Unit = {},
    private val showOrganization: Boolean = false
) : RecyclerView.Adapter<ArchivesAdapter.FileViewHolder>() {

    // Tipos de ViewHolder para diferentes archivos
    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_TEXT = 1
        private const val TYPE_OCR_RESULT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (files[position].type) {
            FileType.IMAGE -> TYPE_IMAGE
            FileType.TEXT -> TYPE_TEXT
            FileType.OCR_RESULT -> TYPE_OCR_RESULT
            else -> TYPE_TEXT // Por defecto
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return when (viewType) {
            TYPE_IMAGE -> ImageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_file_image, parent, false),
                onItemClick
            )
            TYPE_OCR_RESULT -> OcrResultViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_file_ocr_result, parent, false),
                onOcrResultClick
            )
            else -> TextViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_file_ocr_result, parent, false),
                onItemClick
            )
        }
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }

    // ViewHolder base
    abstract class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(file: File)
    }

    // ViewHolder para imágenes
    inner class ImageViewHolder(
        itemView: View,
        private val onClick: (File) -> Unit
    ) : FileViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.tvFileName)
        private val dateTv: TextView = itemView.findViewById(R.id.tvFileDate)
        private val imageIv: ImageView = itemView.findViewById(R.id.ivFileImage)
        private val orgTv: TextView? = itemView.findViewById(R.id.tvOrganization)

        override fun bind(file: File) {
            val imageFile = file as File.ImageFile

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(imageFile.metadata.creationDate) ?: Date()
            dateTv.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)

            orgTv?.text = if (showOrganization) imageFile.metadata.organizationId else ""

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(imageFile.storageInfo.downloadUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(imageIv)

            itemView.setOnClickListener { onClick(imageFile) }
        }
    }

    // ViewHolder para resultados OCR
    inner class OcrResultViewHolder(
        itemView: View,
        private val onClick: (File.TextFile) -> Unit
    ) : FileViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.tvFileName)
        private val contentTv: TextView = itemView.findViewById(R.id.tvContentPreview)
        private val confidenceTv: TextView = itemView.findViewById(R.id.tvConfidence)
        private val dateTv: TextView = itemView.findViewById(R.id.tvProcessingDate)

        override fun bind(file: File) {
            val ocrFile = file as File.TextFile

            nameTv.text = ocrFile.name
            contentTv.text = ocrFile.content.take(100) + if (ocrFile.content.length > 100) "..." else ""
            confidenceTv.text = "Confianza: ${ocrFile.ocrData?.confidence?.times(100)?.toInt()}%"
            ocrFile.ocrData?.processingTimeMs?.let {
                dateTv.text = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    .format(Date(it))
            } ?: run {
                dateTv.text = "Fecha no disponible"
            }

            itemView.setOnClickListener { onClick(ocrFile) }
        }
    }

    // ViewHolder para archivos de texto normales
    inner class TextViewHolder(
        itemView: View,
        private val onClick: (File) -> Unit
    ) : FileViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.tvFileName)
        private val contentTv: TextView = itemView.findViewById(R.id.tvContentPreview)
        private val dateTv: TextView = itemView.findViewById(R.id.tvFileDate)

        override fun bind(file: File) {
            val textFile = file as File.TextFile

            nameTv.text = textFile.name
            contentTv.text = textFile.content.take(150) + if (textFile.content.length > 150) "..." else ""
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(textFile.metadata.creationDate) ?: Date()
            dateTv.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)

            itemView.setOnClickListener { onClick(textFile) }
        }
    }
}