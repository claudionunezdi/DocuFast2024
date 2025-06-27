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
import com.cnunez.docufast.common.adapters.UserAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.manager.GroupManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlin.math.log

class CreateGroupActivity : BaseActivity(), CreateGroupContract.View {

    private lateinit var presenter: CreateGroupContract.Presenter
    private lateinit var userAdapter: UserAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var nameEt: EditText
    private lateinit var descEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_groups)
        setupViews()

        val db = FirebaseDatabase.getInstance()
        val userDao = UserDaoRealtime(db)
        val groupDao = GroupDaoRealtime(db)
        val fileDao = FileDaoRealtime(db)
        val groupManager = GroupManager(groupDao, fileDao)
        val model = CreateGroupModel(userDao, groupDao)
        presenter = CreateGroupPresenter(this, model)

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getById(uid)
                val org = user?.organization
                withContext(Dispatchers.Main) {
                    if (org != null) {
                        presenter.loadUsers(org)
                    } else {
                        showError("No se encontró la organización del usuario")
                    }
                }
            }
        } ?: showError("Usuario no autenticado")

        findViewById<Button>(R.id.buttonCreateGroup).setOnClickListener {
            val name = nameEt.text.toString().trim()
            val desc = descEt.text.toString().trim()
            val selectedUsers = userAdapter.getSelectedUsers()
            presenter.createGroup(name, desc, selectedUsers)
        }
    }

    private fun setupViews() {
        nameEt = findViewById(R.id.editTextGroupName)
        descEt = findViewById(R.id.editTextGroupDescription)
        progressBar = findViewById(R.id.progressBar)

        userAdapter = UserAdapter(emptyList())
        findViewById<RecyclerView>(R.id.recyclerViewUsers).apply {
            layoutManager = LinearLayoutManager(this@CreateGroupActivity)
            adapter = userAdapter
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        // Ya manejado por BaseActivity
    }

    override fun showProgress() {
        progressBar.visibility = ProgressBar.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = ProgressBar.GONE
    }

    override fun showUsers(users: List<User>) {
        userAdapter.setUsers(users)
    }

    override fun onGroupCreated(group: Group) {
        Toast.makeText(this, "Grupo creado: ${group.name}", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onError(message: String) {
        Log.e("CreateGroupActivity: Error en", message)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
