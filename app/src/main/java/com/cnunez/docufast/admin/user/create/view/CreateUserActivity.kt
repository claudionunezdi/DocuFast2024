package com.cnunez.docufast.admin.user.create.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.mainmenu.view.MainMenuActivity
import com.cnunez.docufast.admin.user.create.contract.CreateUserContract
import com.cnunez.docufast.admin.user.create.model.CreateUserModel
import com.cnunez.docufast.admin.user.create.presenter.CreateUserPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.adapters.GroupSelectionAdapter
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.manager.GroupManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CreateUserActivity : BaseActivity(), CreateUserContract.View {

    private lateinit var presenter: CreateUserPresenter
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var workgroupsRecyclerView: RecyclerView
    private lateinit var registerButton: Button
    private lateinit var groupSelectionAdapter: GroupSelectionAdapter

    private var groupsValueEventListener: ValueEventListener? = null
    private var groupsDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        Log.d(TAG, "onCreate - Iniciando actividad")

        initViews()
        setupAdapter()
        setupPresenter()

        FirebaseAuth.getInstance().currentUser?.let {
            onUserAuthenticated(it)
        } ?: run {
            Log.e(TAG, "No hay usuario autenticado")
            finish()
        }
    }

    private fun initViews() {
        Log.d(TAG, "Inicializando vistas")
        usernameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.EmailUserInput) // Asegúrate que coincide con tu XML
        passwordEditText = findViewById(R.id.passwordInput)
        workgroupsRecyclerView = findViewById(R.id.workgroupRecyclerView)
        registerButton = findViewById(R.id.registerButton)
    }

    private fun setupAdapter() {
        Log.d(TAG, "Configurando adapter")
        groupSelectionAdapter = GroupSelectionAdapter().apply {
            setOnGroupSelectedListener { selectedIds ->
                Log.d(TAG, "Grupos seleccionados: ${selectedIds.size}")
            }
        }

        workgroupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CreateUserActivity)
            adapter = groupSelectionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupPresenter() {
        Log.d(TAG, "Configurando presenter")
        val model = CreateUserModel()
        val groupManager = GroupManager() // Asume que tienes esta clase
        presenter = CreateUserPresenter(this, model, groupManager)
    }

    override fun onUserAuthenticated(loggedInUser: FirebaseUser) {
        super.onUserAuthenticated(loggedInUser)
        Log.d(TAG, "Usuario autenticado: ${loggedInUser.uid}")
        loadGroups()
    }

    @SuppressLint("RestrictedApi")
    private fun loadGroups() {
        val currentOrg = SessionManager.getCurrentOrganization()
        if (currentOrg.isNullOrEmpty()) {
            Log.e(TAG, "Organización no asignada al usuario. User: ${SessionManager.getCurrentUser()?.let {
                "id=${it.id}, name=${it.name}, org=${it.organization}, role=${it.role}"
            } ?: "null"}")
            showCreateUserError(getString(R.string.error_no_organization))
            return
        }

        Log.d(TAG, "Iniciando carga de grupos para org: '$currentOrg'")
        Log.d(TAG, "Ruta completa de referencia: ${FirebaseDatabase.getInstance().reference.child("groups").path}")

        groupsDatabaseReference = FirebaseDatabase.getInstance().getReference("groups")
        val query = groupsDatabaseReference!!
            .orderByChild("organization")
            .equalTo(currentOrg)

        Log.d(TAG, "Query construida: orderByChild='organization', equalTo='$currentOrg'")

        groupsValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange recibido. ¿Tiene datos?: ${snapshot.exists()}")
                Log.d(TAG, "Número de hijos: ${snapshot.childrenCount}")

                val groups = snapshot.children.mapNotNull { child ->
                Log.d(TAG, "Procesando hijo: key=${child.key}")
                    try {
                        val group = child.getValue(Group::class.java)?.apply {
                            id = child.key ?: ""
                            Log.d(TAG, "Grupo parseado: id=$id, name=$name, org=$organization")
                        }
                        if (group == null) {
                            Log.w(TAG, "No se pudo parsear el grupo con key: ${child.key}")
                        }
                        group
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando grupo ${child.key}", e)
                        null
                    }
                }

                if (groups.isEmpty()) {
                    Log.w(TAG, "No se encontraron grupos. Posibles causas:",
                        RuntimeException("Debug: Revisar estructura de datos en Firebase"))
                    showCreateUserError(getString(R.string.error_no_groups_available))
                } else {
                    Log.d(TAG, "${groups.size} grupos cargados correctamente. Ejemplo primero: ${
                        groups.firstOrNull()?.let { "id=${it.id}, name=${it.name}" } ?: "N/A"
                    }")
                    groupSelectionAdapter.updateGroups(groups.sortedBy { it.name }, emptySet())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en listener de grupos", error.toException())
                showCreateUserError("Error cargando grupos: ${error.message}")
            }
        }

        query.addValueEventListener(groupsValueEventListener!!)
        Log.d(TAG, "Listener agregado a la query")
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView()
        setupRegisterButtonListener()
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    private fun setupRegisterButtonListener() {
        registerButton.setOnClickListener {
            if (validateInputs()) {
                promptAdminPassword { adminPassword ->
                    presenter.createUserWithAdminPassword(
                        username = usernameEditText.text.toString().trim(),
                        email = emailEditText.text.toString().trim(),
                        password = passwordEditText.text.toString(),
                        workGroupIds = groupSelectionAdapter.getSelectedGroups(),
                        adminPassword = adminPassword
                    )
                }
            }
        }
    }

    private fun promptAdminPassword(onPasswordProvided: (String) -> Unit) {
        val inputEditText = EditText(this).apply {
            hint = getString(R.string.admin_password_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.admin_verification_title))
            .setView(inputEditText)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                inputEditText.text.toString().takeIf { it.isNotEmpty() }?.let(onPasswordProvided)
                    ?: showCreateUserError(getString(R.string.error_admin_password_required))
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun validateInputs(): Boolean {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val hasGroups = groupSelectionAdapter.getSelectedGroups().isNotEmpty()

        when {
            username.isEmpty() -> {
                usernameEditText.error = getString(R.string.error_username_required)
                usernameEditText.requestFocus()
                return false
            }
            email.isEmpty() -> {
                emailEditText.error = getString(R.string.error_email_required)
                emailEditText.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailEditText.error = getString(R.string.error_invalid_email)
                emailEditText.requestFocus()
                return false
            }
            password.isEmpty() -> {
                passwordEditText.error = getString(R.string.error_password_required)
                passwordEditText.requestFocus()
                return false
            }
            password.length < 6 -> {
                passwordEditText.error = getString(R.string.error_password_too_short)
                passwordEditText.requestFocus()
                return false
            }
            !hasGroups -> {
                showCreateUserError(getString(R.string.error_group_selection_required))
                return false
            }
        }

        return true
    }

    override fun showCreateUserSuccess() {
        Toast.makeText(this, getString(R.string.success_user_created), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun showCreateUserError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onStop() {
        super.onStop()
        groupsValueEventListener?.let { listener ->
            groupsDatabaseReference?.removeEventListener(listener)
        }
        groupsValueEventListener = null
    }

    companion object {
        private const val TAG = "CreateUserActivity"
    }
}