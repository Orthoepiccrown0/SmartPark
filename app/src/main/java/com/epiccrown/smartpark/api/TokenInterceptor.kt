package com.epiccrown.smartpark.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
//        val user = MyPreferences.getUser(context)
//        if (user != null) {
//            val request = chain.request().newBuilder()
//            request.addHeader("Authorization", "Bearer ${user.token}")
//            return chain.proceed(request.build())
//        }
        return chain.proceed(chain.request())
    }
}