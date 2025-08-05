package com.cnunez.docufast.user.mainmenu.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.adapters.UserGroupAdapter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.cnunez.docufast.user.mainmenu.Presenter.MainMenuUserPresenter
import com.cnunez.docufast.user.group.detail.view.GroupDetailActivity
import com.google.firebase.auth.FirebaseUser

class MainMenuUserActivity : BaseActivity(), MainMenuUserContract.View, UserGroupAdapter.OnItemClickListener {

    private lateinit var presenter: MainMenuUserPresenter
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var groupAdapter: UserGroupAdapter
    private var currentGroup: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_user)

        handleGroupFromIntent()


        setupRecyclerView()
        setupGroupInfoView()
        presenter = MainMenuUserPresenter(this)
    }

    override fun onStart() {
        super.onStart()
        // Verificar autenticación y cargar grupos
        val user = SessionManager.getCurrentUser()
        if (user != null) {
            presenter.loadUserGroups()
        } else {
            showError("Usuario no autenticado")
            finish()
        }
    }

    override fun onUserAuthenticated(user: FirebaseUser) {
        if (user.isAnonymous) {
            showError("User is not authenticated")
            finish()
        } else {
            presenter.loadUserGroups()
        }
    }

    private fun setupRecyclerView() {
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
        groupAdapter = UserGroupAdapter(emptyList(), this)
        groupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainMenuUserActivity)
            adapter = groupAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleGroupFromIntent() {
        currentGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("group", Group::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("group")
        }

        currentGroup?.let {
            showCurrentGroupInfo(it)
        }
    }

    private fun setupGroupInfoView() {
        // Configurar vista para mostrar el grupo actual
        findViewById<View>(R.id.currentGroupContainer)?.setOnClickListener {
            currentGroup?.let { group ->
                showGroupDetail(group)
            }
        }
    }

    private fun showCurrentGroupInfo(group: Group) {
        runOnUiThread {
            findViewById<TextView>(R.id.textCurrentGroupName)?.text = group.name
            findViewById<TextView>(R.id.textCurrentGroupMembers)?.text =
                "Miembros: ${group.members.size}"
            findViewById<TextView>(R.id.textCurrentGroupFiles)?.text =
                "Archivos: ${group.files.size}"

            // Mostrar el contenedor
            findViewById<View>(R.id.currentGroupContainer)?.visibility = View.VISIBLE
        }
    }

    override fun showGroups(groups: List<Group>) {
        Log.d("GROUP_DEBUG", "Grupos recibidos en UI: ${groups.size}")
        runOnUiThread {
            if (groups.isEmpty()) {
                showError("No tienes grupos disponibles")
                // Mostrar estado vacío si es necesario
            } else {
                groups.forEach {
                    Log.d("GROUP_DEBUG", "Grupo: ${it.id} - ${it.name} - Miembros: ${it.members.size}")
                }
                groupAdapter.setGroups(groups)
            }
        }
    }

    override fun showError(message: String) {
        Log.e("MAIN_MENU_ERROR", message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Opcional: Mostrar un mensaje más amigable para el usuario
            val userMessage = when {
                message.contains("no existe") -> "Algunos de tus grupos ya no están disponibles"
                else -> message
            }
            Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun showGroupDetail(group: Group) {
        val intent = Intent(this, GroupDetailActivity::class.java).apply {
            putExtra("group", group)
            putExtra("organizationId", SessionManager.getCurrentUser()?.organization)
        }
        startActivity(intent)
    }

    override fun onGroupClick(group: Group) {
        presenter.onGroupSelected(group)
    }
}