package com.cnunez.docufast

import android.app.Application
import com.cnunez.docufast.common.firebase.AppDatabase
import com.cnunez.docufast.common.firebase.PhotoDaoFirebase
import com.cnunez.docufast.common.firebase.TextFileDaoFirebase
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        // Inicializa Firebase
        FirebaseApp.initializeApp(this)

        // Inicializa la base de datos aquí
        database = object : AppDatabase() {
            override fun photoDao(): PhotoDaoFirebase {
                // Devuelve una instancia de PhotoDaoFirebase
                return PhotoDaoFirebase()
            }

            override fun textFileDao(): TextFileDaoFirebase {
                // Devuelve una instancia de TextFileDaoFirebase
                return TextFileDaoFirebase()
            }
        }
    }
}