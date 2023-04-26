package com.epiccrown.smartpark.repository

import com.epiccrown.smartpark.model.request.ProcessDataRequest
import com.epiccrown.smartpark.model.response.ProcessDataResponse
import com.epiccrown.smartpark.repository.network.BaseRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class AdminRepository() : BaseRepository.Unauthenticated("api/") {
    private val service = getService<AdminService>()
    suspend fun processData(request: ProcessDataRequest): NetworkResult<ProcessDataResponse> {
        return safeApiCall { service.processData(request) }
    }

    interface AdminService {
        @POST("process-data")
        suspend fun processData(@Body request: ProcessDataRequest): Response<ProcessDataResponse>

    }
}