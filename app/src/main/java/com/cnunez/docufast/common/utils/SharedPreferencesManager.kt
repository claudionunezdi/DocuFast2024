package com.cnunez.docufast.common.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {
    private const val PREF_NAME = "UserPrefs"
    private const val KEY_USER_ROLE = "userRole"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserRole(context: Context, role: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_USER_ROLE, role)
        editor.apply()
    }

    fun getUserRole(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_ROLE, null)
    }

    fun clearUserRole(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_USER_ROLE)
        editor.apply()
    }
}