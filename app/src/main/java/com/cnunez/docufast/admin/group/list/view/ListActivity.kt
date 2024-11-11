package com.cnunez.docufast.admin.group.list.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.cnunez.docufast.admin.group.list.contract.ListContract
import com.cnunez.docufast.admin.group.list.model.ListModel
import com.cnunez.docufast.admin.group.list.presenter.ListPresenter

class ListActivity : AppCompatActivity(), ListContract.View {

    private lateinit var presenter: ListContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list_groups)

        presenter = ListPresenter(this, ListModel())
        presenter.loadGroups()
    }

    override fun showGroups(groups: List<WorkGroup>) {

        for (group in groups) {
            Toast.makeText(this, "Group: ${group.name}, Users: ${group.members.size}, Files: ${group.files.size}", Toast.LENGTH_LONG).show()
        }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}