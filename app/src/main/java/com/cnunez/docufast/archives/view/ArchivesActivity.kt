// ArchivesActivity.kt
package com.cnunez.docufast.archives.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.archives.contract.ArchivesContract
import com.cnunez.docufast.archives.model.ArchivesModel
import com.cnunez.docufast.archives.presenter.ArchivesPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.fileContent.view.FileContentActivity
import com.google.firebase.auth.FirebaseUser

/**
 * Activity para visualizar archivos completos (imagen + texto) de un grupo.
 */// ArchivesActivity.kt
class ArchivesActivity : BaseActivity(), ArchivesContract.View {
    private lateinit var presenter: ArchivesContract.Presenter
    private lateinit var adapter: FileAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_archives)

        recyclerView = findViewById(R.id.recyclerView)
        presenter = ArchivesPresenter(
            model   = ArchivesModel(),
            view    = this,
            context = this
        )
        presenter.start()
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        if (user.isAnonymous) {
            Toast.makeText(this, "Anonymous user detected. Redirecting to login.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Welcome ${user.email}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showFiles(files: List<File>) {
        adapter = FileAdapter(
            files,
            onOpenClick       = { presenter.openFile(it) },
            onEditClick       = { presenter.editFile(it) },
            onViewContentClick= { presenter.viewFileContent(it) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter        = adapter
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Ya **SIN** override
    fun openFile(file: File) {
        presenter.openFile(file)
    }

    fun editFile(file: File) {
        presenter.editFile(file)
    }

    fun viewFileContent(file: File) {
        startActivity(Intent(this, FileContentActivity::class.java)
            .putExtra("FILE_ID", file.id))
    }
}
