package com.cnunez.docufast

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class MyApp : Application() {
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firestore: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        // Inicializa Firebase
        FirebaseApp.initializeApp(this)

        // Configuración de Firebase Realtime Database
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.setPersistenceEnabled(true) // Habilita el modo offline

        // Configuración de Firestore
        firestore = FirebaseFirestore.getInstance()

        // Verifica conectividad con Firebase
        if (isOnline()) {
            checkFirebaseConnection()
        } else {
            Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkFirebaseConnection() {
        val databaseReference = firebaseDatabase.getReference(".info/connected")
        databaseReference.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Toast.makeText(this@MyApp, "Conectado a Firebase", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MyApp, "Desconectado de Firebase", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(this@MyApp, "Error al verificar conexión con Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}