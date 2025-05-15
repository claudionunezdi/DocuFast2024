package com.cnunez.docufast.common.utils

import android.content.Context
import android.content.SharedPreferences

object Utils {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ROLE = "user_role"

    fun saveUserRole(context: Context, role: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ROLE, role)
        editor.apply()
    }

    fun getUserRole(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }
}