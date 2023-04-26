package com.epiccrown.smartpark.api

import android.content.Context
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {
    private val pf = UserPreferences(context)
    override fun intercept(chain: Interceptor.Chain): Response {
        val user = pf.getUser()
        if (user != null) {
            val request = chain.request().newBuilder()
            request.addHeader("Authorization", "Bearer ${user.token}")
            return chain.proceed(request.build())
        }
        return chain.proceed(chain.request())
    }
}