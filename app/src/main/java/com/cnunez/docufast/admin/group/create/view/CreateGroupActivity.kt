package com.cnunez.docufast.admin.group.create.view

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.admin.group.create.model.CreateGroupModel
import com.cnunez.docufast.admin.group.create.presenter.CreateGroupPresenter
import com.cnunez.docufast.common.adapters.UserAdapterUnified
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.View.VISIBLE
import android.view.View.GONE
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.LENGTH_LONG
import com.cnunez.docufast.common.base.SessionManager

class CreateGroupActivity : BaseActivity(), CreateGroupContract.View {
    private lateinit var presenter: CreateGroupPresenter
    private lateinit var userAdapter: UserAdapterUnified
    private lateinit var progressBar: ProgressBar
    private lateinit var nameEt: EditText
    private lateinit var descEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_groups)

        setupViews()
        setupPresenter()
        loadUsers()
        setupCreateButton()
    }

    private fun setupViews() {
        nameEt = findViewById(R.id.editTextGroupName)
        descEt = findViewById(R.id.editTextGroupDescription)
        progressBar = findViewById(R.id.progressBar)

        userAdapter = UserAdapterUnified(mutableListOf(), UserAdapterUnified.Mode.SELECTION)
        findViewById<RecyclerView>(R.id.recyclerViewUsers).apply {
            layoutManager = LinearLayoutManager(this@CreateGroupActivity)
            adapter = userAdapter
        }
    }

    private fun setupPresenter() {
        val db = FirebaseDatabase.getInstance()
        val model = CreateGroupModel(
            UserDaoRealtime(db),
            GroupDaoRealtime(db)
        )
        presenter = CreateGroupPresenter(this, model)
    }

    private fun loadUsers() {
        SessionManager.getCurrentUser()?.organization?.let { org ->
            presenter.loadUsers(org)
        } ?: showError("No se pudo obtener la organización")
    }

    private fun setupCreateButton() {
        findViewById<Button>(R.id.buttonCreateGroup).setOnClickListener {
            val name = nameEt.text.toString().trim()
            val desc = descEt.text.toString().trim()
            val selectedUsers = userAdapter.getSelectedUsers()

            presenter.createGroup(name, desc, selectedUsers)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    // Resto de implementaciones de la interfaz View...
    override fun showProgress() { progressBar.visibility = VISIBLE }
    override fun hideProgress() { progressBar.visibility = GONE }
    override fun showUsers(users: List<User>) { userAdapter.updateUsers(users) }
    override fun onGroupCreated(group: Group) {
        Toast.makeText(this, "Grupo ${group.name} creado", LENGTH_SHORT).show()
        finish()
    }
    override fun showError(message: String) { Toast.makeText(this, message, LENGTH_LONG).show() }
    override fun onError(message: String) { Log.e("CreateGroup", message) }
}