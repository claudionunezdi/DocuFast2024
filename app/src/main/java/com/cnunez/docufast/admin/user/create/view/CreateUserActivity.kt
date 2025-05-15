package com.cnunez.docufast.admin.user.create.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.admin.user.create.model.CreateUserModel
import com.cnunez.docufast.admin.user.create.presenter.CreateUserPresenter
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.adapters.UserAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.auth.FirebaseUser

class CreateUserActivity : BaseActivity(),  CreateUserContract.View, GroupAdapter.OnItemClickListener {
    private lateinit var presenter: CreateUserContract.Presenter
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var workgroupRecyclerView: RecyclerView
    private lateinit var registerButton: Button
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var userAdapter: UserAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val selectedWorkgroups = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        presenter = CreateUserPresenter(this, CreateUserModel(this))
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.UserInputButton)
        emailEditText = findViewById(R.id.EmailUserInput)
        passwordEditText = findViewById(R.id.PasswordUserInput)
        workgroupRecyclerView = findViewById(R.id.workgroupRecyclerView)
        registerButton = findViewById(R.id.registerButton)

        groupAdapter = GroupAdapter(emptyList(), this)
        workgroupRecyclerView.layoutManager = LinearLayoutManager(this)
        workgroupRecyclerView.adapter = groupAdapter

        userAdapter = UserAdapter(mutableListOf())

        registerButton.setOnClickListener {
            showPasswordPrompt { adminPassword ->
                val username = usernameEditText.text.toString()
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                presenter.createUserWithAdminPassword(username, email, password, selectedWorkgroups, adminPassword)
            }
        }

        loadWorkgroups()
    }

    private fun loadWorkgroups() {
        db.collection("groups").get()
            .addOnSuccessListener { result ->
                val workgroups = result.map { it.getString("name") ?: "" }
                groupAdapter.setGroups(workgroups.map { Group(name = it) })
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting workgroups: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPasswordPrompt(onPasswordEntered: (String) -> Unit) {
        val passwordInput = EditText(this).apply {
            hint = "Ingrese su contraseña"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirmación de contraseña")
            .setMessage("Por favor, ingrese su contraseña para continuar.")
            .setView(passwordInput)
            .setPositiveButton("Aceptar") { _, _ ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) {
                    onPasswordEntered(password)
                } else {
                    Toast.makeText(this, "Debe ingresar una contraseña", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    override fun onOpenGroupClick(group: Group) {
        if (selectedWorkgroups.contains(group.name)) {
            selectedWorkgroups.remove(group.name)
        } else {
            selectedWorkgroups.add(group.name)
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        Toast.makeText(this, "Usuario autenticado: ${user.uid}", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteGroupClick(group: Group) {}

    override fun showCreateUserSuccess() {
        Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun showCreateUserError(message: String) {
        Toast.makeText(this, "Error creating user: $message", Toast.LENGTH_SHORT).show()
    }


}