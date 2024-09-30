package com.cnunez.docufast.archives.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.archives.contract.ArchivesContract
import com.cnunez.docufast.archives.model.impl.ArchivesModelImpl
import com.cnunez.docufast.archives.presenter.ArchivesPresenter
import com.cnunez.docufast.fileContent.view.FileContentActivity
import java.io.File

class ArchivesActivity: AppCompatActivity(), ArchivesContract.View {
    private lateinit var presenter: ArchivesContract.Presenter
    private lateinit var adapter: FileAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_archives)
        recyclerView = findViewById(R.id.recyclerView)
        presenter = ArchivesPresenter(ArchivesModelImpl(this), this)
        presenter.listFiles()
    }

    override fun showFiles(files: List<File>) {
        adapter = FileAdapter(files) { file ->
            presenter.openFile(file)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun openFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.fromFile(file), "text/plain")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun editFile(file: File) {
        presenter.editFile(file)
    }

    override fun viewFileContent(file: File){
        val intent = Intent(this, FileContentActivity::class.java)
        intent.putExtra("filePath", file.absolutePath)
        startActivity(intent)
    }


}