package com.cnunez.docufast.admin.group.edit.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.group.create.view.CreateGroupActivity
import com.cnunez.docufast.admin.group.edit.contract.ListContract
import com.cnunez.docufast.admin.group.edit.model.ListModel
import com.cnunez.docufast.admin.group.edit.presenter.ListPresenter
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.auth.FirebaseUser

class ListActivity : BaseActivity(), ListContract.View, GroupAdapter.OnItemClickListener {

    private  var presenter: ListContract.Presenter? = null
    private lateinit var recyclerViewGroups: RecyclerView
    private lateinit var groupsAdapter: GroupAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list_groups)
        println("onCreate() - Inicio")

        recyclerViewGroups = findViewById(R.id.recyclerViewGroups)
        groupsAdapter = GroupAdapter(mutableListOf(), this)
        recyclerViewGroups.layoutManager = LinearLayoutManager(this)
        recyclerViewGroups.adapter = groupsAdapter
        val fabAddGroup = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddGroup)

        presenter = ListPresenter(this, ListModel(this))
        println("Presenter inicializado correctamente")
        println("onCreate() - Fin")

        checkUserAuthentication()

        fabAddGroup.setOnClickListener {
            // Navegar a la actividad de creación de grupo
            val intent = Intent(this, CreateGroupActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onUserAuthenticated(user: FirebaseUser) {

        println("onUserAuthenticated llamado con usuario: ${user.email}")
        if (user.isAnonymous) {
            showError("User is not authenticated")
            println("onUserAuthenticated() - Fin")
            finish()
        } else {

            presenter?.loadGroups() // Usa el operador safe call
            println("onUserAuthenticated() - Fin")
        }
    }

    override fun showGroups(groups: List<Group>) {
        groupsAdapter.setGroups(groups)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    // Métodos de la interfaz GroupAdapter.OnItemClickListener
    override fun onOpenGroupClick(group: Group) {
        // Lógica para abrir el grupo
        Toast.makeText(this, "Abriendo grupo: ${group.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteGroupClick(group: Group) {
        // Lógica para eliminar el grupo
        Toast.makeText(this, "Eliminando grupo: ${group.name}", Toast.LENGTH_SHORT).show()
    }
}