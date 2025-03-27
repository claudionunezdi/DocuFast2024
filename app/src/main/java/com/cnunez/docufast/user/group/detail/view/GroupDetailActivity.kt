package com.cnunez.docufast.user.group.detail.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.camera.view.CameraActivity
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.user.adapters.FileAdapter
import com.cnunez.docufast.user.file.detail.view.FileDetailActivity
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.user.group.detail.presenter.GroupDetailPresenter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class GroupDetailActivity : AppCompatActivity(), GroupDetailContract.View {

    private lateinit var presenter: GroupDetailPresenter
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var fabAddFile: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_group_detail)

        val group: Group = intent.getParcelableExtra("group")!!
        val organizationId = intent.getStringExtra("organizationId") ?: return

        setupRecyclerView()
        setupFab()

        presenter = GroupDetailPresenter(this)
        presenter.loadGroupFiles(group.id, organizationId)
    }

    private fun setupRecyclerView() {
        filesRecyclerView = findViewById(R.id.recyclerViewFiles)
        fileAdapter = FileAdapter { file -> presenter.onFileSelected(file) }
        filesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailActivity)
            adapter = fileAdapter
        }
    }

    private fun setupFab() {
        fabAddFile = findViewById(R.id.fabAddFile)
        fabAddFile.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        }
    }

    override fun showFiles(files: List<TextFile>) {
        fileAdapter.setFiles(files)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showFileDetail(file: TextFile) {
        val intent = Intent(this, FileDetailActivity::class.java).apply {
            putExtra("file", file)
            putExtra("organizationId", file.organizationId)
        }
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE_CAMERA = 1
    }
}