package com.cnunez.docufast.loginMenu.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.mainmenu.View.MainMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginMenuActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                userId?.let {
                    db.collection("users").document(it).get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role")
                            if (role == "admin") {
                                val intent = Intent(this, MainMenuActivity::class.java)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this, com.cnunez.docufast.user.mainmenu.View.MainMenuActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(this, "User document does not exist", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}