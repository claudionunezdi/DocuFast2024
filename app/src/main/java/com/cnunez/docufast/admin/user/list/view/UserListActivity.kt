package com.cnunez.docufast.admin.user.list.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.list.contract.UserListContract
import com.cnunez.docufast.admin.user.list.model.UserListModel
import com.cnunez.docufast.admin.user.list.presenter.UserListPresenter
import com.cnunez.docufast.common.adapters.UserListAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.cnunez.docufast.common.dataclass.User



class UserListActivity : BaseActivity(), UserListContract.View {
    private val presenter: UserListContract.Presenter by lazy {
        UserListPresenter(this, UserListModel())
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        recyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(mutableListOf()) { user ->
            presenter.deleteUser(user.id) // Llama al método del presenter con el ID del usuario
        }
        recyclerView.adapter = adapter

        checkUserAuthentication()
    }

    override fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadOrganizationAndUsers(currentUser)
        } else {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { loadOrganizationAndUsers(it) }
                } else {
                    showError("Authentication failed.")
                }
            }
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        if (user.isAnonymous) {
            Toast.makeText(this, "Anonymous user detected. Redirecting to login.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Welcome ${user.email}", Toast.LENGTH_SHORT).show()
            // Aquí puedes cargar la lista de usuarios o cualquier otra acción necesaria
            loadOrganizationAndUsers(user)
        }
    }

    private fun loadOrganizationAndUsers(currentUser: FirebaseUser) {
        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            val organization = document.getString("organization") ?: ""
            presenter.loadUsers(organization)
        }.addOnFailureListener { exception ->
            showError(exception.message ?: "Error loading organization")
        }
    }

    override fun showUsers(users: List<User>) {
        adapter.updateUsers(users)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}