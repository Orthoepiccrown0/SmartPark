package com.epiccrown.smartpark.model.request

data class LoginRequest(
    val phone: String = "",
    val plate: String = ""
)