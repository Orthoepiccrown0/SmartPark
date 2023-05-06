package com.epiccrown.smartpark.model.request

data class CarRevealedRequest(
    val idPark: Int = 0,
    val plate: String = ""
)