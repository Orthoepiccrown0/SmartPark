package com.epiccrown.smartpark.repository.network

import com.epiccrown.smartpark.model.response.ErrorResponse

sealed class NetworkResult<T>() {

    // We'll wrap our data in this 'Success'
    // class in case of success response from api
    class Success<T>(val data: T?) : NetworkResult<T>()

    // We'll pass error message wrapped in this 'Error'
    // class to the UI in case of failure response
    class Error<T>(val exception: ErrorResponse) : NetworkResult<T>()

    // We'll just pass object of this Loading
    // class, just before making an api call
    class Loading<T> : NetworkResult<T>()
}