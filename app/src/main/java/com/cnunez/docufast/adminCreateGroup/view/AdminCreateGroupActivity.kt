package com.cnunez.docufast.createGroupAdmin.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.WorkGroup
import com.cnunez.docufast.adminCreateGroup.contract.AdminCreateGroupContract
import com.cnunez.docufast.adminCreateGroup.presenter.AdminCreateGroupAdminPresenter
import com.cnunez.docufast.common.dataclass.Admin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class AdminCreateGroupActivity : AppCompatActivity(), AdminCreateGroupContract.View {

    private lateinit var presenter: AdminCreateGroupAdminPresenter
    private lateinit var editTextGroupName: EditText
    private lateinit var editTextGroupDescription: EditText
    private lateinit var buttonCreateGroup: Button
    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val selectedUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_groups)

        val adminUser = Admin(
            id = 1,
            name = "Admin",
            email = "admin@example.com",
            organization = "AdminOrg",
            password = "admin123"
        )

        presenter = AdminCreateGroupAdminPresenter(this)

        editTextGroupName = findViewById(R.id.editTextGroupName)
        editTextGroupDescription = findViewById(R.id.editTextGroupDescription)
        buttonCreateGroup = findViewById(R.id.buttonCreateGroup)
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)

        // Initialize RecyclerView
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        val users = getUsers() // Function to get the list of users
        userAdapter = UserAdapter(users, selectedUsers)
        recyclerViewUsers.adapter = userAdapter

        buttonCreateGroup.setOnClickListener {
            val name = editTextGroupName.text.toString()
            val description = editTextGroupDescription.text.toString()
            val members = selectedUsers.toMutableList()
            members.addAll(adminUser.users) // Add admin's users to the group
            presenter.createGroup(name, description, members)
        }

        // Add users to the admin's list
        users.forEach { user ->
            addUserToAdmin(adminUser, user)
        }
    }

    override fun getUsers(): List<User> {
        val db = FirebaseFirestore.getInstance()
        val users = mutableListOf<User>()

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    users.add(user)
                }
            }
            .addOnFailureListener { exception ->
                onError("Error getting users: ${exception.message}")
            }

        return users
    }

    override fun addUserToAdmin(adminUser: Admin, user: User) {
        adminUser.users.add(user)
    }

    override fun onGroupCreated(group: WorkGroup) {
        Toast.makeText(this, "Group created: ${group.name}", Toast.LENGTH_SHORT).show()

    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }
}


