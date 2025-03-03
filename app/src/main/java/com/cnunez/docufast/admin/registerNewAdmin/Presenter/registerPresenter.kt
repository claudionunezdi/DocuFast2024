package com.cnunez.docufast.admin.registerNewAdmin.Presenter

    import com.cnunez.docufast.admin.registerNewAdmin.Contract.registerContract
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore

    class registerPresenter(private val view: registerContract.View, private val model: registerContract.Model) : registerContract.Presenter {

        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        override fun register(fullName: String, email: String, password: String, organization: String) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val userData = hashMapOf(
                                "fullName" to fullName,
                                "email" to email,
                                "organization" to organization,
                                "role" to "admin",
                                "workGroups" to emptyList<String>(),
                                "stability" to 0
                            )
                            db.collection("users").document(user.uid).set(userData)
                                .addOnSuccessListener {
                                    view.showRegisterSuccess()
                                }
                                .addOnFailureListener { e ->
                                    view.showRegisterError("Failed to save user data: ${e.message}")
                                }
                        } else {
                            view.showRegisterError("User registration failed.")
                        }
                    } else {
                        view.showRegisterError("Registration failed: ${task.exception?.message}")
                    }
                }
        }
    }