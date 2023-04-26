package com.epiccrown.smartpark.model.response

data class ErrorResponse(
    val code: String?,
) {


    companion object {
        const val TIMEOUT = "TIMEOUT"

        fun getGeneric(): ErrorResponse {
            return ErrorResponse(ErrorCodes.GENERIC.code)
        }

        fun serverError(): ErrorResponse {
            return ErrorResponse(ErrorCodes.SERVER_ERROR.code )
        }
    }

    enum class ErrorCodes(val code:String){
        GENERIC("G007"),
        SERVER_ERROR("S007"),

    }
}
