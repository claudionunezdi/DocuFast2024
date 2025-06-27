package com.cnunez.docufast.admin.user.create.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.admin.user.create.model.CreateUserModel
import com.cnunez.docufast.admin.user.create.presenter.CreateUserPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.adapters.GroupAdapter
import com.cnunez.docufast.common.dataclass.Group
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class CreateUserActivity : BaseActivity(), CreateUserContract.View, GroupAdapter.OnItemClickListener {

    private lateinit var presenter: CreateUserContract.Presenter
    private lateinit var usernameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var btnRegister: Button
    private lateinit var groupAdapter: GroupAdapter
    private val selectedGroups = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        // Vistas
        usernameEt = findViewById(R.id.fullNameEditText)
        emailEt    = findViewById(R.id.EmailUserInput)
        passwordEt = findViewById(R.id.passwordInput)
        recycler   = findViewById(R.id.workgroupRecyclerView)
        btnRegister= findViewById(R.id.registerButton)

        // Adapter de grupos
        groupAdapter = GroupAdapter(emptyList(), this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = groupAdapter

        // Modelo y Presentador
        val model = CreateUserModel()
        presenter = CreateUserPresenter(this, model)
    }

    /**
     * Callback de BaseActivity: el usuario ya está autenticado y tiene rol validado.
     */
    override fun onUserAuthenticated(user: FirebaseUser) {
        // Ahora podemos cargar la lista de grupos
        loadGroups()
    }

    /** Carga todos los grupos disponibles para que el admin los seleccione */
    private fun loadGroups() {
        FirebaseDatabase.getInstance().getReference("groups")
            .get().addOnSuccessListener { snap ->
                val list = snap.children.mapNotNull {
                    it.getValue(Group::class.java)?.apply { id = it.key.orEmpty() }
                }
                groupAdapter.setGroups(list)
            }.addOnFailureListener { e ->
                showCreateUserError("Error cargando grupos: ${e.message}")
            }
    }

    /** Muestra un prompt para verificar contraseña de admin antes de crear usuario */
    private fun promptAdminPassword(onPass: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = "Contraseña admin"
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        AlertDialog.Builder(this)
            .setTitle("Verificación Admin")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                input.text.toString()
                    .takeIf { it.isNotEmpty() }
                    ?.let(onPass)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Grupo clickeado: alternamos selección */
    override fun onOpenGroupClick(group: Group) {
        if (selectedGroups.contains(group.id)) selectedGroups.remove(group.id)
        else selectedGroups.add(group.id)
    }

    override fun onGroupClick(group: Group) {
        // No se utiliza en este flujo


    }

    override fun onDeleteClick(group: Group) {
        // No se utiliza en este flujo

    }

    override fun onDeleteGroupClick(group: Group) {
        // No se utiliza en este flujo
    }

    /** Vista: éxito en creación */
    override fun showCreateUserSuccess() {
        Toast.makeText(this, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** Vista: error en creación */
    override fun showCreateUserError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
    }

    /** Configuración del botón tras autenticar */
    override fun onResume() {
        super.onResume()
        // Al pulsar registro, solicitamos contraseña admin y creamos el usuario
        btnRegister.setOnClickListener {
            promptAdminPassword { adminPass ->
                presenter.createUserWithAdminPassword(
                    usernameEt.text.toString().trim(),
                    emailEt.text.toString().trim(),
                    passwordEt.text.toString(),
                    selectedGroups,
                    adminPass
                )
            }
        }
    }
}
