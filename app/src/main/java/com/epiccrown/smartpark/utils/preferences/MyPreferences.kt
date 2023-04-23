package com.epiccrown.smartpark.utils.preferences

import android.content.Context
import android.content.SharedPreferences

abstract class MyPreferences(private val context: Context,private val prefs: String) {
    fun getPrefs(): SharedPreferences? {
        return context.getSharedPreferences(prefs, Context.MODE_PRIVATE)
    }

}