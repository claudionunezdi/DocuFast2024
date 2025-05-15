package com.cnunez.docufast.admin.group.create.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.admin.group.create.model.CreateGroupModel
import com.cnunez.docufast.admin.group.create.presenter.CreateGroupPresenter
import com.cnunez.docufast.common.adapters.UserAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.user.login.view.LoginUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class CreateGroupActivity : BaseActivity(), CreateGroupContract.View {

    private lateinit var presenter: CreateGroupPresenter
    private lateinit var editTextGroupName: EditText
    private lateinit var editTextGroupDescription: EditText
    private lateinit var buttonCreateGroup: Button
    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var model: CreateGroupContract.Model
    // Referencia a la base de datos
    private val database = FirebaseDatabase.getInstance().reference
    private var adminUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_groups)
        model = CreateGroupModel(this)
        presenter = CreateGroupPresenter(this,model)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Redirigir a login si no hay usuario autenticado
            startActivity(Intent(this, LoginUserActivity::class.java))
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        fetchAdminUser(currentUser.uid)
    }

    private fun initViews() {
        editTextGroupName = findViewById(R.id.editTextGroupName)
        editTextGroupDescription = findViewById(R.id.editTextGroupDescription)
        buttonCreateGroup = findViewById(R.id.buttonCreateGroup)
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)

        buttonCreateGroup.setOnClickListener {
            val name = editTextGroupName.text.toString()
            val description = editTextGroupDescription.text.toString()
            val members = userAdapter.getSelectedUsers()

            if (name.isBlank() || description.isBlank()) {
                showError("Nombre y descripción son obligatorios")
                return@setOnClickListener
            }

            presenter.createGroup(name, description, members)
        }
    }

    private fun setupRecyclerView() {
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(mutableListOf())
        recyclerViewUsers.adapter = userAdapter
    }

    private fun fetchAdminUser(userId: String) {
        database.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    adminUser = snapshot.getValue(User::class.java)
                    adminUser?.let {
                        if (it.role == "admin") {
                            getUsersFromOrganization(it.organization)
                        } else {
                            showError("No tienes permisos de administrador")
                            finish()
                        }
                    } ?: run {
                        showError("Usuario no encontrado")
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Error al obtener usuario: ${error.message}")
                    finish()
                }
            })
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        // Implementación de BaseActivity
        fetchAdminUser(user.uid)
    }

    override fun onGroupCreated(group: Group) {
        Toast.makeText(this, "Grupo ${group.name} creado", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun getUsersFromOrganization(organization: String) {
        database.child("users").orderByChild("organization").equalTo(organization)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = mutableListOf<User>()
                    for (userSnapshot in snapshot.children) {
                        userSnapshot.getValue(User::class.java)?.let { users.add(it) }
                    }
                    userAdapter.setUsers(users)
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Error al obtener usuarios: ${error.message}")
                }
            })
    }
}