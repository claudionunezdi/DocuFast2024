package com.cnunez.docufast.archives.Model.impl


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import java.io.File
import com.cnunez.docufast.archives.Model.ArchivesModel


class ArchivesModelImpl(private val context: Context) : ArchivesModel {



    override fun listArchives(): List<File> {
        val directory = context.getFilesDir()

        Log.d("ArchivesModelImpl: Directory Path: ", directory.toString())
        return directory.listFiles { _, name -> name.endsWith(".txt") }?.toList() ?: emptyList()
    }


    override fun openFile(file: File) {
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.fromFile(file), "text/plain")
            startActivity(context, this, null)
        }
    }

    override fun editFile(file: File) {
        Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(Uri.fromFile(file), "text/plain")
            startActivity(context, this, null)
        }
    }


}