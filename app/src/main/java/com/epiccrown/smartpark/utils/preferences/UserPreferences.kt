package com.epiccrown.smartpark.utils.preferences

import android.content.Context
import com.epiccrown.smartpark.model.internal.AdminConfiguration
import com.epiccrown.smartpark.model.response.UserResponse
import com.google.gson.Gson
import java.lang.Exception

class UserPreferences(context: Context) : MyPreferences(context, "user_prefs") {

    companion object {
        private const val USER_ADMIN = "USER_ADMIN"
        private const val USER_MODEL = "USER_MODEL"
        private const val USER_TYPE_SELECTED = "USER_TYPE_SELECTED"
        private const val ADMIN_CONFIG = "ADMIN_CONFIG"
    }

    fun setUser(user: UserResponse) {
        getPrefs()?.edit()?.putString(USER_MODEL, Gson().toJson(user))?.apply()
    }

    fun getUser(): UserResponse? {
        return try {
            Gson().fromJson(getPrefs()?.getString(USER_MODEL, null), UserResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun setAdminConfig(configuration: AdminConfiguration) {
        getPrefs()?.edit()?.putString(ADMIN_CONFIG, Gson().toJson(configuration))?.apply()
    }

    fun getAdminConfiguration(): AdminConfiguration? {
        return try {
            Gson().fromJson(
                getPrefs()?.getString(ADMIN_CONFIG, null),
                AdminConfiguration::class.java
            )
        } catch (e: Exception) {
            null
        }
    }

    fun isUserTypeSelected(): Boolean {
        return getPrefs()?.getBoolean(USER_TYPE_SELECTED, false) ?: false
    }

    fun isAdmin(): Boolean {
        return getPrefs()?.getBoolean(USER_ADMIN, true) ?: true
    }

    fun setAdmin(value: Boolean) {
        getPrefs()?.edit()?.putBoolean(USER_ADMIN, value)?.apply()
        getPrefs()?.edit()?.putBoolean(USER_TYPE_SELECTED, true)?.apply()
    }

    fun clear() {
        getPrefs()?.edit()?.clear()?.apply()
    }
}