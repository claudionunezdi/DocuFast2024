package com.cnunez.docufast.admin.user.create.View

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.create.Contract.CreateUserContract
import com.cnunez.docufast.admin.user.create.Model.CreateUserModel
import com.cnunez.docufast.admin.user.create.Presenter.CreateUserPresenter
import com.cnunez.docufast.common.Utils
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.adapters.UserAdapter


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.manager.UserManager

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
    private lateinit var userManager: UserManager
    private val selectedWorkgroups = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        presenter = CreateUserPresenter(this, CreateUserModel(this))
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userManager = UserManager()

        usernameEditText = findViewById(R.id.UserInputButton)
        emailEditText = findViewById(R.id.EmailUserInput)
        passwordEditText = findViewById(R.id.PasswordUserInput)
        workgroupRecyclerView = findViewById(R.id.workgroupRecyclerView)
        registerButton = findViewById(R.id.registerButton)

        groupAdapter = GroupAdapter(emptyList(), this)
        workgroupRecyclerView.layoutManager = LinearLayoutManager(this)
        workgroupRecyclerView.adapter = groupAdapter

        userAdapter = UserAdapter(mutableListOf())
        // Assuming you have a RecyclerView for users, set it up similarly
        // val userRecyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
        // userRecyclerView.layoutManager = LinearLayoutManager(this)
        // userRecyclerView.adapter = userAdapter

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val role = "user"
            val currentUser = auth.currentUser

            currentUser?.let {
                db.collection("users").document(it.uid).get().addOnSuccessListener { document ->
                    val userRole = Utils.getUserRole(this)
                    val isAdmin = userRole == "admin"
                    Log.d("UserRoleCheck", "Is user admin: $isAdmin")

                    if (isAdmin) {
                        val organization = document.getString("organization") ?: ""
                        Log.d("CreateUser", "Checking if email is already in use: $email")
                        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result?.signInMethods
                                if (signInMethods.isNullOrEmpty()) {
                                    Log.d("CreateUser", "Email not in use, creating user: $email")
                                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { createTask ->
                                        if (createTask.isSuccessful) {
                                            val newUser = auth.currentUser
                                            newUser?.let { newUser ->
                                                val user = User(
                                                    id = newUser.uid,
                                                    name = username,
                                                    email = email,
                                                    password = password,
                                                    organization = organization,
                                                    workGroups = selectedWorkgroups,
                                                    role = role
                                                )
                                                db.collection("users").document(newUser.uid).set(user)
                                                    .addOnSuccessListener {
                                                        showCreateUserSuccess()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        showCreateUserError(e.message ?: "Unknown error")
                                                    }
                                            }
                                        } else {
                                            Log.e("CreateUser", "Error creating user: ${createTask.exception?.message}")
                                            showCreateUserError(createTask.exception?.message ?: "Error creating user")
                                        }
                                    }
                                } else {
                                    Log.e("CreateUser", "The email address is already in use by another account.")
                                    showCreateUserError("The email address is already in use by another account.")
                                }
                            } else {
                                Log.e("CreateUser", "Error checking email: ${task.exception?.message}")
                                showCreateUserError(task.exception?.message ?: "Error checking email")
                            }
                        }
                    } else {
                        Log.e("CreateUser", "No tienes permisos suficientes para crear un usuario")
                        showCreateUserError("No tienes permisos suficientes para crear un usuario")
                    }
                }
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

    override fun onOpenGroupClick(group: Group) {
        if (selectedWorkgroups.contains(group.name)) {
            selectedWorkgroups.remove(group.name)
        } else {
            selectedWorkgroups.add(group.name)
        }
    }

    override fun onDeleteGroupClick(group: Group) {

    }

    override fun showCreateUserSuccess() {
        Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun showCreateUserError(message: String) {
        Toast.makeText(this, "Error creating user: $message", Toast.LENGTH_SHORT).show()
    }
}