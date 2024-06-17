package com.cnunez.docufast


import android.app.Application
import androidx.room.Room
import com.cnunez.docufast.useCamera.Model.AppDatabase

class MyApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app-database"
        ).build()
    }
}