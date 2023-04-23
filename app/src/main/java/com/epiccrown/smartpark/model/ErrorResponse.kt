package com.example.baseapp.model

data class ErrorResponse(
    val errorCode: String?,
    val error: String?
) {
    override fun toString(): String {
        return error?:"Generic error"
    }

    companion object {
        const val TIMEOUT = "TIMEOUT"

        fun getGeneric(): ErrorResponse {
            return ErrorResponse("KO", "Generic error")
        }

        fun serverError(): ErrorResponse {
            return ErrorResponse("KO", "Server error, please retry again later")
        }
    }
}
