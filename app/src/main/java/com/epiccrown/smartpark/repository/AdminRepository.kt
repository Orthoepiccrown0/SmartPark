package com.epiccrown.smartpark.repository

import com.epiccrown.smartpark.model.request.AddParkRequest
import com.epiccrown.smartpark.model.request.AddZoneRequest
import com.epiccrown.smartpark.model.request.CarRevealedRequest
import com.epiccrown.smartpark.model.request.ProcessDataRequest
import com.epiccrown.smartpark.model.response.AllZonesResponse
import com.epiccrown.smartpark.model.response.ProcessDataResponse
import com.epiccrown.smartpark.model.response.SuccessResponse
import com.epiccrown.smartpark.repository.network.BaseRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class AdminRepository() : BaseRepository.Unauthenticated("api/") {
    private val service = getService<AdminService>()
    suspend fun processData(request: ProcessDataRequest): NetworkResult<ProcessDataResponse> {
        return safeApiCall { service.processData(request) }
    }

    suspend fun getAllZones():NetworkResult<AllZonesResponse>{
        return safeApiCall { service.getAllZones() }
    }

    suspend fun addPark(request: AddParkRequest):NetworkResult<SuccessResponse>{
        return safeApiCall { service.addPark(request) }
    }

    suspend fun addZone(request: AddZoneRequest):NetworkResult<SuccessResponse>{
        return safeApiCall { service.addZone(request) }
    }

    suspend fun carRevealed(request: CarRevealedRequest):NetworkResult<SuccessResponse>{
        return safeApiCall { service.carRevealed(request) }
    }

    interface AdminService {
        @POST("process-data")
        suspend fun processData(@Body request: ProcessDataRequest): Response<ProcessDataResponse>

        @GET("getAllZones")
        suspend fun getAllZones(): Response<AllZonesResponse>

        @POST("addPark")
        suspend fun addPark(@Body request: AddParkRequest): Response<SuccessResponse>

        @POST("addZone")
        suspend fun addZone(@Body request: AddZoneRequest): Response<SuccessResponse>

        @POST("addLotWithCar")
        suspend fun carRevealed(@Body request: CarRevealedRequest): Response<SuccessResponse>

    }
}