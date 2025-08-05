package com.cnunez.docufast

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.SharedPreferences
import android.util.Log
import com.cnunez.docufast.common.base.SessionManager
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.utils.GroupSyncUtil
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.initialize
import com.google.firebase.analytics.ktx.analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MyApp : Application() {

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var sharedPrefs: SharedPreferences

    companion object {
        private const val TAG = "MyApp"
        lateinit var instance: MyApp
            private set
        private const val GROUPS_SYNC_KEY = "groups_synced_v1"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPrefs = getSharedPreferences("app_migrations", MODE_PRIVATE)
        initializeApp()
        //runOneTimeGroupSync()
    }

    private fun initializeApp() {
        appScope.launch {
            try {
                initializeFirebase()
                setupDatabase()
                checkAuthState()
                setupAnalytics()
            } catch (e: Exception) {
                Log.e(TAG, "Initialization error", e)
            }
        }
    }

    /*private fun runOneTimeGroupSync() {
        if (BuildConfig.DEBUG && !sharedPrefs.getBoolean(GROUPS_SYNC_KEY, false)) {
            appScope.launch {
                try {
                    Log.i(TAG, "Starting group synchronization...")
                    GroupSyncUtil.syncAllUsersGroups()
                    sharedPrefs.edit().putBoolean(GROUPS_SYNC_KEY, true).apply()
                    Log.i(TAG, "Group synchronization completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Group synchronization failed", e)
                }
            }
        }
    }*/

    private fun initializeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialized")
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Firebase init error", e)
        }
    }

    private fun setupDatabase() {
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            listOf("users", "groups", "files").forEach { path ->
                getReference(path).keepSynced(true)
            }
        }
    }

    private fun checkAuthState() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            auth.currentUser?.let { firebaseUser ->
                // Limpiar cache antes de recuperar sesión
                if (SessionManager.getCurrentUserId() != firebaseUser.uid) {
                    SessionManager.clearSession()
                }

                recoverUserSession(firebaseUser.uid)
            } ?: run {
                SessionManager.logout()
            }
        }
    }

    private fun recoverUserSession(userId: String) {
        FirebaseDatabase.getInstance()
            .getReference("users/$userId")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.getValue(User::class.java)?.let {
                    SessionManager.saveUserSession(it)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to recover user session", it)
            }
    }

    private fun setupAnalytics() {
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
    }

    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            appScope.cancel("Memory cleanup")
        }
        super.onTrimMemory(level)
    }
}

