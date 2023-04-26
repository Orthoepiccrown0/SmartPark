package com.epiccrown.smartpark.repository.network

import android.content.Context
import android.util.Log
import com.epiccrown.smartpark.BuildConfig
import com.epiccrown.smartpark.api.ContentTypeInterceptor
import com.epiccrown.smartpark.api.TokenInterceptor
import com.epiccrown.smartpark.model.response.ErrorResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException

sealed class BaseRepository(val context: Context? = null, val suffix: String = "") {

    open class Authenticated(context: Context, suffix: String) : BaseRepository(context, suffix)
    open class Unauthenticated(suffix: String) : BaseRepository(suffix = suffix)

    inline fun <reified T> getService(): T {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient().newBuilder()
        httpClient
            .addInterceptor(ContentTypeInterceptor())
            .addInterceptor(interceptor)
        if (context != null)
            httpClient.addInterceptor(TokenInterceptor(context))
        httpClient.retryOnConnectionFailure(true)

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BuildConfig.BASE_URL + suffix)
            .client(httpClient.build())
            .build()
        Log.e("SERVICE", "created ${T::class.simpleName}")
        return retrofit.create(T::class.java)
    }

    suspend fun <T> safeApiCall(apiToBeCalled: suspend () -> Response<T>): NetworkResult<T> {

        // Returning api response
        // wrapped in Resource class
        return withContext(Dispatchers.IO) {
            try {

                // Here we are calling api lambda
                // function that will return response
                // wrapped in Retrofit's Response class
                val response: Response<T> = apiToBeCalled()

                if (response.isSuccessful) {
                    // In case of success response we
                    // are returning Resource.Success object
                    // by passing our data in it.
                    NetworkResult.Success(data = response.body()!!)
                } else {
                    // parsing api's own custom json error
                    // response in ExampleErrorResponse pojo
                    val errorResponse: ErrorResponse? =
                        convertErrorBody(response.errorBody())
                    // Simply returning api's own failure message
                    NetworkResult.Error(
                        errorResponse ?: ErrorResponse.getGeneric()
                    )
                }

            } catch (e: HttpException) {
                // Returning HttpException's message
                // wrapped in Resource.Error
                NetworkResult.Error(ErrorResponse.serverError())
            } catch (e: IOException) {
                // Returning no internet message
                // wrapped in Resource.Error
                NetworkResult.Error(ErrorResponse.serverError())
            } catch (e: Exception) {
                // Returning 'Something went wrong' in case
                // of unknown error wrapped in Resource.Error
                NetworkResult.Error(ErrorResponse.getGeneric())
            }
        }
    }

    private fun convertErrorBody(errorBody: ResponseBody?): ErrorResponse? {
        return try {
            Gson().fromJson(errorBody?.source().toString(), ErrorResponse::class.java)
        } catch (exception: Exception) {
            null
        }
    }
}
