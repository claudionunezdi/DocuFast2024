package com.cnunez.docufast.admin.user.detail.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import com.cnunez.docufast.admin.user.detail.model.UserDetailModel
import com.cnunez.docufast.admin.user.detail.presenter.UserDetailPresenter

import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.adapters.GroupSelectionAdapter
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseUser

class UserDetailActivity : BaseActivity(), UserDetailContract.View {

    private lateinit var presenter: UserDetailPresenter
    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var saveBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var groupsRecyclerView: RecyclerView

    private lateinit var groupSelectionAdapter: GroupSelectionAdapter
    private val selectedGroupIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        Log.d("UserDetailActivity", "onCreate")

        bindViews()
        setupGroupsRecyclerView()
        setupMVP()
    }

    private fun bindViews() {
        nameEt = findViewById(R.id.editTextName)
        emailEt = findViewById(R.id.editTextEmail)
        passwordEt = findViewById(R.id.editTextPassword)
        saveBtn = findViewById(R.id.buttonSave)
        progressBar = findViewById(R.id.progressBar)
        groupsRecyclerView = findViewById(R.id.recyclerViewGroups)

        saveBtn.setOnClickListener {
            saveUserChanges()
        }
    }

    private fun setupGroupsRecyclerView() {
        groupSelectionAdapter = GroupSelectionAdapter().apply {
            setOnGroupSelectedListener { selectedIds ->
                selectedGroupIds.clear()
                selectedGroupIds.addAll(selectedIds)
            }
        }

        groupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserDetailActivity)
            adapter = groupSelectionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupMVP() {
        presenter = UserDetailPresenter(this, UserDetailModel())
    }

    private fun getUserIdFromIntent(): String {
        return intent.getStringExtra("USER_ID")?.takeIf { it.isNotEmpty() }
            ?: run {
                val errorMsg = "ID de usuario inválido o vacío"
                onError(errorMsg)
                Log.e("UserDetailActivity", errorMsg)
                finish()
                "" // Retorno vacío que no se usará porque finish() cierra la actividad
            }
    }

    private fun saveUserChanges() {
        presenter.updateUser(
            name = nameEt.text.toString().trim(),
            email = emailEt.text.toString().trim().takeIf { it.isNotBlank() }, // Opcional
            selectedGroupIds = selectedGroupIds.toList(),
            newPassword = passwordEt.text.toString().takeIf { it.isNotBlank() } // Opcional
        )
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        Log.d("UserDetailActivity", "User authenticated, loading user data")
        val userId = getUserIdFromIntent()
        if (userId.isNotEmpty()) {
            presenter.loadUser(userId)
        }
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun displayUser(user: User) {
        Log.d("UserDetailActivity", "Displaying user: ${user.name} (${user.id})")
        nameEt.setText(user.name)
        emailEt.setText(user.email)
        selectedGroupIds.clear()
        selectedGroupIds.addAll(user.workGroups.keys)
    }

    override fun displayGroups(groups: List<Group>, selectedIds: Set<String>) {
        Log.d("UserDetailActivity", "Displaying ${groups.size} groups")
        groupSelectionAdapter.updateGroups(groups, selectedIds)
    }
        override fun onUpdateSuccess() {
            Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
        }

        override fun onError(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        companion object {
            fun start(context: Context, userId: String) {
                val intent = Intent(context, UserDetailActivity::class.java)
                intent.putExtra("USER_ID", userId)
                context.startActivity(intent)
            }
        }
    }
