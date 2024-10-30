package com.cnunez.docufast.adminListGroup.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.cnunez.docufast.adminListGroup.contract.AdminListGroupContract
import com.cnunez.docufast.adminListGroup.model.AdminListGroupModel
import com.cnunez.docufast.adminListGroup.presenter.AdminListGroupPresenter

class AdminListGroupActivity : AppCompatActivity(), AdminListGroupContract.View {

    private lateinit var presenter: AdminListGroupContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list_groups)

        presenter = AdminListGroupPresenter(this, AdminListGroupModel())
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