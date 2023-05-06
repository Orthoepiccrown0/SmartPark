package com.epiccrown.smartpark.repository

import android.content.Context
import com.epiccrown.smartpark.model.request.AddCarRequest
import com.epiccrown.smartpark.model.request.SuccessPaymentRequest
import com.epiccrown.smartpark.model.response.ErrorResponse
import com.epiccrown.smartpark.model.response.HistoryResponse
import com.epiccrown.smartpark.model.response.HomeResponse
import com.epiccrown.smartpark.model.response.SuccessResponse
import com.epiccrown.smartpark.model.response.UserInfoResponse
import com.epiccrown.smartpark.repository.network.BaseRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class UserRepository(context: Context) : BaseRepository.Authenticated(context, "api/") {
    private val service = getService<UserService>()
    suspend fun getHomePage(): NetworkResult<HomeResponse> {
        return safeApiCall { service.getHomeData() }
    }

    suspend fun getUserInfo(): NetworkResult<UserInfoResponse> {
        return safeApiCall { service.userInfo() }
    }

    suspend fun getHistory(): NetworkResult<HistoryResponse> {
        return safeApiCall { service.getPaymentHistory() }
    }

    suspend fun successPayment(request: SuccessPaymentRequest): NetworkResult<SuccessResponse> {
        return safeApiCall { service.successPayment(request) }
    }
    suspend fun addCar(plate: String): NetworkResult<SuccessResponse> {
        if (context == null)
            return NetworkResult.Error(ErrorResponse.getGeneric())
        val pf = UserPreferences(context)
        val user = pf.getUser() ?: return NetworkResult.Error(ErrorResponse.getGeneric())

        return safeApiCall { service.addCar(AddCarRequest(user.idUser.toString(), plate)) }
    }

    interface UserService {
        @GET("getHomepage")
        suspend fun getHomeData(): Response<HomeResponse>

        @GET("getPaymentHistory")
        suspend fun getPaymentHistory(): Response<HistoryResponse>

        @GET("userInfo")
        suspend fun userInfo(): Response<UserInfoResponse>

        @POST("addCar")
        suspend fun addCar(@Body request: AddCarRequest): Response<SuccessResponse>

        @POST("successPayment")
        suspend fun successPayment(@Body request: SuccessPaymentRequest): Response<SuccessResponse>
    }
}