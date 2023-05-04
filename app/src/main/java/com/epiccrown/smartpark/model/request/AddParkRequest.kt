package com.epiccrown.smartpark.model.request

import java.math.BigDecimal

data class AddParkRequest(
    val zoneId: Int,
    val parkName: String,
    val price: BigDecimal,
    val minTime: Int,
    val slots: Int,
)
