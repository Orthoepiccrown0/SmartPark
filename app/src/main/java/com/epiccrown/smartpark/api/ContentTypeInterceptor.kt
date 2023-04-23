package com.epiccrown.smartpark.api

import okhttp3.Interceptor
import okhttp3.Response

class ContentTypeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        request.addHeader("Content-Type", "application/json")
        request.addHeader("Accept", "application/json")
        return chain.proceed(request.build())
    }
}