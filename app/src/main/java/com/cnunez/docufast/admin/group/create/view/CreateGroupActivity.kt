package com.cnunez.docufast.admin.group.create.view

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.adapters.UserAdapter
import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.admin.group.create.presenter.CreateGroupPresenter
import com.cnunez.docufast.common.manager.UserManager
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class CreateGroupActivity : AppCompatActivity(), CreateGroupContract.View {

    private lateinit var presenter: CreateGroupPresenter
    private lateinit var editTextGroupName: EditText
    private lateinit var editTextGroupDescription: EditText
    private lateinit var buttonCreateGroup: Button
    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val selectedUsers = mutableListOf<User>()
    private val userManager = UserManager() // Instanciar UserManager
    private lateinit var adminUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_groups)

        presenter = CreateGroupPresenter(this)

        editTextGroupName = findViewById(R.id.editTextGroupName)
        editTextGroupDescription = findViewById(R.id.editTextGroupDescription)
        buttonCreateGroup = findViewById(R.id.buttonCreateGroup)
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)

        // Inicializar RecyclerView
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(mutableListOf(), selectedUsers)
        recyclerViewUsers.adapter = userAdapter

        buttonCreateGroup.setOnClickListener {
            val name = editTextGroupName.text.toString()
            val description = editTextGroupDescription.text.toString()
            val members = selectedUsers.toMutableList()
            val files = listOf<File>() // Reemplazar con la lista de archivos real
            presenter.createGroup(name, description, members, files)
        }

        fetchAdminUser()
    }

    private fun fetchAdminUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val email = currentUser.email
            val db = FirebaseFirestore.getInstance()
            db.collection("users").whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        adminUser = result.documents[0].toObject(User::class.java)!!
                        if (adminUser.role.equals("admin", ignoreCase = true)) {
                            getUsersFromOrganization(adminUser.organization)
                        } else {
                            Log.d("CreateGroupActivity", "No tienes permisos suficientes")
                        }
                    } else {
                        Log.d("CreateGroupActivity", "Usuario administrador no encontrado")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("CreateGroupActivity", "Error al obtener el usuario administrador: ${exception.message}")
                }
        } else {
            Log.d("CreateGroupActivity", "No se encontró un usuario autenticado")
        }
    }

    override fun getUsersFromOrganization(organization: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").whereEqualTo("organization", organization)
            .get()
            .addOnSuccessListener { result ->
                val users = result.map { document -> document.toObject(User::class.java) }
                userAdapter.setUsers(users)
            }
            .addOnFailureListener { exception ->
                onError("Error getting users: ${exception.message}")
            }
    }

    override fun onGroupCreated(group: WorkGroup) {
        Toast.makeText(this, "Group created: ${group.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}