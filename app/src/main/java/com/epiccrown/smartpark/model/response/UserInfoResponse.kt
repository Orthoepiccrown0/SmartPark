package com.epiccrown.smartpark.model.response

data class UserInfoResponse(
    val cars: List<Car> = listOf(),
    val idUser: Int = 0,
    val phone: String = ""
) {
    data class Car(
        val balance: String = "",
        val plate: String = ""
    )
}