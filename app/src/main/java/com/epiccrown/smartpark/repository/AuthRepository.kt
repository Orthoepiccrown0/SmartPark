package com.epiccrown.smartpark.repository

import com.epiccrown.smartpark.model.request.LoginRequest
import com.epiccrown.smartpark.model.response.UserResponse
import com.epiccrown.smartpark.repository.network.BaseRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import kotlinx.coroutines.delay
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class AuthRepository() : BaseRepository.Unauthenticated("api/") {

    private val service = getService<AuthService>()

    suspend fun login(request: LoginRequest): NetworkResult<UserResponse> {
        return safeApiCall { service.login(request) }
    }

    interface AuthService {
        @POST("login")
        suspend fun login(@Body request: LoginRequest): Response<UserResponse>
    }
}