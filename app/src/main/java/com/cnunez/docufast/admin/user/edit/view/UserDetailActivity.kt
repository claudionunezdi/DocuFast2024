package com.cnunez.docufast.admin.user.edit.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.detail.contract.UserDetailContract
import com.cnunez.docufast.admin.user.detail.presenter.UserDetailPresenter
import com.cnunez.docufast.admin.user.edit.model.UserDetailModel
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseUser

class UserDetailActivity : BaseActivity(),
    UserDetailContract.View,
    GroupAdapter.OnItemClickListener {

    private lateinit var presenter: UserDetailContract.Presenter
    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var saveBtn: Button
    private val selectedGroups = mutableSetOf<String>()
    private lateinit var groupAdapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        bindViews()
        setupMVP()
    }

    private fun bindViews() {
        nameEt   = findViewById(R.id.editTextName)
        emailEt  = findViewById(R.id.editTextEmail)
        recycler = findViewById(R.id.recyclerViewGroups)
        saveBtn  = findViewById(R.id.buttonSave)

        groupAdapter = GroupAdapter(emptyList(), this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = groupAdapter

        saveBtn.setOnClickListener {
            presenter.updateUser(
                nameEt.text.toString().trim(),
                emailEt.text.toString().trim(),
                selectedGroups.toList()
            )
        }
    }

    private fun setupMVP() {
        presenter = UserDetailPresenter(this, UserDetailModel())
    }

    private fun getUserIdFromIntent(): String =
        intent.getStringExtra("USER_ID")?.takeIf { it.isNotEmpty() }
            ?: run {
                showError("Usuario inválido")
                finish()
                ""
            }

    override fun onUserAuthenticated(user: FirebaseUser) {
        presenter.loadUser(getUserIdFromIntent())
    }

    override fun showProgress() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }

    override fun hideProgress() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }

    override fun displayUser(user: User) {
        nameEt.setText(user.name)
        emailEt.setText(user.email)
        selectedGroups.clear()
        selectedGroups.addAll(user.workGroups.keys)
    }

    override fun displayGroups(groups: List<Group>, selectedIds: Set<String>) {
        groupAdapter.setGroups(groups)
        selectedGroups.clear()
        selectedGroups.addAll(selectedIds)
    }

    override fun onUpdateSuccess() {
        Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onOpenGroupClick(group: Group) {
        if (!selectedGroups.add(group.id)) {
            selectedGroups.remove(group.id)
        }
    }

    override fun onGroupClick(group: Group) {

    }

    override fun onDeleteClick(group: Group) {
        TODO("Not yet implemented")
    }

    override fun onDeleteGroupClick(group: Group) {
        // No se utiliza en esta actividad
    }
}
