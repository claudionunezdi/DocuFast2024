package com.cnunez.docufast.admin.adminregister.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.adminregister.Contract.RegisterUserContract
import com.cnunez.docufast.admin.adminregister.model.RegisterUserModel
import com.cnunez.docufast.admin.adminregister.presenter.RegisterUserPresenter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterUserActivity : AppCompatActivity(), RegisterUserContract.View {

    private lateinit var presenter: RegisterUserPresenter
    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button
    private var adminOrganization: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        presenter = RegisterUserPresenter(this, RegisterUserModel())

        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonRegister = findViewById(R.id.buttonRegister)

        // Obtener la organización del administrador
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val adminId = auth.currentUser?.uid

        if (adminId != null) {
            db.collection("admins").document(adminId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        adminOrganization = document.getString("organization")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get admin organization: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        buttonRegister.setOnClickListener {
            val fullName = editTextFullName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val organization = adminOrganization ?: ""
            presenter.register(fullName, email, password, organization)
        }
    }

    override fun showRegisterSuccess() {
        Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun showRegisterError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}