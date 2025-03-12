package com.cnunez.docufast.admin.user.create.view

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.admin.user.create.model.CreateUserModel
import com.cnunez.docufast.admin.user.create.presenter.CreateUserPresenter
import com.cnunez.docufast.common.Utils
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.adapters.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cnunez.docufast.common.dataclass.Group

class CreateUserActivity : AppCompatActivity(), CreateUserContract.View, GroupAdapter.OnItemClickListener {
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
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            presenter.createUser(username, email, password, selectedWorkgroups)
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

    override fun onOpenGroupClick(group: Group) {
        if (selectedWorkgroups.contains(group.name)) {
            selectedWorkgroups.remove(group.name)
        } else {
            selectedWorkgroups.add(group.name)
        }
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