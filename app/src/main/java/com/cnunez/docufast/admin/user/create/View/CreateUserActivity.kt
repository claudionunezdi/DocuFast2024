package com.cnunez.docufast.admin.user.create.View

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.user.create.MVP.CreateUserPresenter
import com.cnunez.docufast.user.create.MVP.CreateUserContract
import com.cnunez.docufast.user.create.MVP.CreateUserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateUserActivity : AppCompatActivity(), CreateUserContract.View {
    private lateinit var presenter: CreateUserContract.Presenter
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var workgroupRecyclerView: RecyclerView
    private lateinit var registerButton: Button
    private lateinit var workgroupAdapter: WorkgroupAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        presenter = CreateUserPresenter(this, CreateUserModel())
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.UserInputButton)
        emailEditText = findViewById(R.id.EmailUserInput)
        passwordEditText = findViewById(R.id.PasswordUserInput)
        workgroupRecyclerView = findViewById(R.id.workgroupRecyclerView)
        registerButton = findViewById(R.id.registerButton)

        // Simulación de grupos de trabajo (opcional)
        val workgroups = listOf("Group A", "Group B", "Group C")
        workgroupAdapter = WorkgroupAdapter(workgroups)
        workgroupRecyclerView.layoutManager = LinearLayoutManager(this)
        workgroupRecyclerView.adapter = workgroupAdapter

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val selectedWorkgroups = workgroupAdapter.getSelectedWorkgroups()
            val role = "user"
            val currentUser = auth.currentUser

            currentUser?.let {
                db.collection("users").document(it.uid).get().addOnSuccessListener { document ->
                    val organization = document.getString("organization") ?: ""
                    (presenter as? CreateUserPresenter)?.createUser(username, email, password, selectedWorkgroups, organization, role)
                }
            }
        }
    }

    override fun showCreateUserSuccess() {
        Toast.makeText(this, "Register successful", Toast.LENGTH_SHORT).show()
    }

    override fun showCreateUserError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}