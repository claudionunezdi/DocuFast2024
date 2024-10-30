package com.cnunez.docufast.registerNewAdmin.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.adminLogin.view.LoginAdminActivity
import com.cnunez.docufast.registerNewAdmin.Contract.RegisterNewAdminContract
import com.cnunez.docufast.registerNewAdmin.Model.RegisterNewAdminModel
import com.cnunez.docufast.registerNewAdmin.Presenter.RegisterNewAdminPresenter
import com.google.firebase.auth.FirebaseAuth

class RegisterNewAdminActivity : AppCompatActivity(), RegisterNewAdminContract.View {
    private lateinit var presenter: RegisterNewAdminContract.Presenter
    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var organizationEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var adminText: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_new_admin)
        auth = FirebaseAuth.getInstance()

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        organizationEditText = findViewById(R.id.organizationEditText)
        registerButton = findViewById(R.id.registerButton)
        adminText = findViewById(R.id.registerNewAdminTextView)

        presenter = RegisterNewAdminPresenter(this, RegisterNewAdminModel())

        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val organization = organizationEditText.text.toString()
            presenter.register(fullName, email, password, organization)
        }
    }

    override fun showRegisterSuccess() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Registration successful. User ID: ${currentUser.uid}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginAdminActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Registration successful, but user is not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showRegisterError(message: String) {
        Toast.makeText(this, "Registration failed: $message", Toast.LENGTH_SHORT).show()
    }
}