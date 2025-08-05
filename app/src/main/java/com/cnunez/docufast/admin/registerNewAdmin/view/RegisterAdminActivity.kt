// RegisterAdminActivity.kt
package com.cnunez.docufast.admin.registerNewAdmin.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.registerNewAdmin.contract.RegisterAdminContract
import com.cnunez.docufast.admin.registerNewAdmin.presenter.RegisterAdminPresenter
import com.cnunez.docufast.admin.registerNewAdmin.model.RegisterAdminModel
import com.cnunez.docufast.user.login.view.LoginUserActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase

class RegisterAdminActivity : AppCompatActivity(), RegisterAdminContract.View {

    private lateinit var presenter: RegisterAdminContract.Presenter
    private lateinit var fullNameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var organizationEt: EditText
    private lateinit var registerBtn: Button
    val db = FirebaseDatabase.getInstance()





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_new_admin)

        fullNameEt     = findViewById(R.id.fullNameEditText)
        emailEt        = findViewById(R.id.emailEditText)
        passwordEt     = findViewById(R.id.passwordEditText)
        organizationEt = findViewById(R.id.organizationEditText)
        registerBtn    = findViewById(R.id.registerButton)
        Log.d("RTDB", "URL de la base: ${db.reference.root.toString()}")

        // Creamos el Presenter pasándole esta Activity (View) y un modelo nuevo
        presenter = RegisterAdminPresenter(this, RegisterAdminModel())

        registerBtn.setOnClickListener {
            val fullName     = fullNameEt.text.toString().trim()
            val email        = emailEt.text.toString().trim()
            val password     = passwordEt.text.toString().trim()
            val organization = organizationEt.text.toString().trim()

            // Llamamos al método que registra un Administrador
            presenter.registerAdmin(fullName, email, password, organization)
        }
    }

    // --- Métodos del contrato para registrar Admin ---
    override fun showLoading() {
        // Aquí podrías mostrar un ProgressBar (por ejemplo)
    }

    override fun hideLoading() {
        // Ocultar el ProgressBar
    }



    override fun showRegisterSuccess() {
        Log.d("RTDB", "URL de la base: ${db.reference.root.toString()}")
        Snackbar.make(registerBtn, "Administrador creado exitosamente", Snackbar.LENGTH_LONG)
            .show()
        // Redirigir a login para que el nuevo Admin se autentique:
        startActivity(Intent(this, LoginUserActivity::class.java))
        finish()

    }

    override fun showRegisterError(message: String) {
        Log.d("RTDB", "URL de la base: ${db.reference.root.toString()}")
        Snackbar.make(registerBtn, message, Snackbar.LENGTH_LONG).show()
    }

    // Estos dos métodos quedan vacíos aquí, porque CreateUserActivity los usará:
    override fun showUserCreateSuccess() { }
    override fun showUserCreateError(message: String) { }
}
