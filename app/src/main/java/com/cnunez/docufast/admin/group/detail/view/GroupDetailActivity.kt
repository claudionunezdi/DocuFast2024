package com.cnunez.docufast.admin.group.detail.view

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailActivity : AppCompatActivity(), GroupDetailContract.View {

    private lateinit var presenter: GroupDetailContract.Presenter
    private lateinit var textViewGroupName: TextView
    private lateinit var textViewGroupDescription: TextView
    private lateinit var textViewGroupMembers: TextView
    private lateinit var textViewGroupFiles: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        textViewGroupName = findViewById(R.id.textViewGroupName)
        textViewGroupDescription = findViewById(R.id.textViewGroupDescription)
        textViewGroupMembers = findViewById(R.id.textViewGroupMembers)
        textViewGroupFiles = findViewById(R.id.textViewGroupFiles)

        presenter = GroupDetailPresenter(this)

        val groupId = intent.getStringExtra("groupId")
        if (groupId != null) {
            presenter.loadGroupDetails(groupId)
        } else {
            Toast.makeText(this, "Invalid group ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun showGroupDetails(group: WorkGroup) {
        textViewGroupName.text = group.name
        textViewGroupDescription.text = group.description
        textViewGroupMembers.text = group.members.joinToString(", ") { it.name }
        textViewGroupFiles.text = group.files.joinToString(", ") { it.name }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}