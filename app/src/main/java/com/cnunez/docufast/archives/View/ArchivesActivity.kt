package com.cnunez.docufast.archives.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.archives.Contract.ArchivesContract
import com.cnunez.docufast.archives.Model.impl.ArchivesModelImpl
import com.cnunez.docufast.archives.Presenter.ArchivesPresenter
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
        presenter.openFile(file)
    }

    override fun editFile(file: File) {
        presenter.editFile(file)
    }

}