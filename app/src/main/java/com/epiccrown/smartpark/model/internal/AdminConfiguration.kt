package com.epiccrown.smartpark.model.internal

import com.epiccrown.smartpark.model.response.AvailableZonesResponse

data class AdminConfiguration(
    val selectedZone: Zone,
    val selectedPark: Park,
) {
    data class Zone(
        val idZone: Int = 0,
        val zoneDescription: String = "",
        val zoneName: String = "",
    )

    data class Park(
        val idPark: Int = 0,
        val minTime: Int = 0,
        val name: String = "",
        val price: String = "",
        val slots: Int = 0,
        val visible: Int = 0
    )
}