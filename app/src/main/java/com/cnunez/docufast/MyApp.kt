package com.cnunez.docufast

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1) Inicializa Firebase Core
        FirebaseApp.initializeApp(this)

        // 2) Configura Realtime Database: persistencia offline + sincronización de ramas clave
        val rtdb = FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
        }
        rtdb.getReference("users").keepSynced(true)
        rtdb.getReference("groups").keepSynced(true)
        rtdb.getReference("files").keepSynced(true)

        // 3) (Opcional) Aquí ya no hacemos toasts ni comprobaciones de conectividad,
        //    porque esas deben vivir en Activities o en un NetworkMonitor dedicado.
    }
}