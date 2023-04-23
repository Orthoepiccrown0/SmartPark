package com.epiccrown.smartpark.repository

import com.epiccrown.smartpark.model.ProcessDataRequest
import com.epiccrown.smartpark.model.ProcessDataResponse
import com.epiccrown.smartpark.repository.network.NetworkResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class Repository : BaseRepository("api/") {
    suspend fun processData(request: ProcessDataRequest): NetworkResult<ProcessDataResponse> {
        return safeApiCall { getService<DataService>().processData(request) }
    }

    interface DataService {
        @POST("process-data")
        suspend fun processData(@Body request: ProcessDataRequest): Response<ProcessDataResponse>
    }
}