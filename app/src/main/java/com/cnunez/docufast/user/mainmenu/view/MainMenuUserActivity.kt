package com.cnunez.docufast.user.mainmenu.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.adapters.UserGroupAdapter
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.cnunez.docufast.user.mainmenu.Presenter.MainMenuUserPresenter
import com.cnunez.docufast.user.group.detail.view.GroupDetailActivity

class MainMenuUserActivity : AppCompatActivity(), MainMenuUserContract.View, UserGroupAdapter.OnItemClickListener {

    private lateinit var presenter: MainMenuUserPresenter
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var groupAdapter: UserGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_user)

        setupRecyclerView()
        presenter = MainMenuUserPresenter(this)
        presenter.loadUserGroups()
    }

    private fun setupRecyclerView() {
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
        groupAdapter = UserGroupAdapter(this)
        groupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainMenuUserActivity)
            adapter = groupAdapter
        }
    }

    override fun showGroups(groups: List<Group>) {
        groupAdapter.setGroups(groups)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showGroupDetail(group: Group) {
        val intent = Intent(this, GroupDetailActivity::class.java).apply {
            putExtra("groupId", group.id)
            putExtra("groupName", group.name)
        }
        startActivity(intent)
    }

    override fun onGroupClick(group: Group) {
        presenter.onGroupSelected(group)
    }
}