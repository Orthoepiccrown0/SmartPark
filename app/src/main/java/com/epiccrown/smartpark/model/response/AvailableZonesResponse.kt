package com.epiccrown.smartpark.model.response

data class AvailableZonesResponse(
    val zones: List<Zone> = listOf()
) {
    data class Zone(
        val idZone: Int = 0,
        val parks: List<Park> = listOf(),
        val zoneDescription: String = "",
        val zoneName: String = ""
    ) {
        data class Park(
            val idPark: Int = 0,
            val minTime: Int = 0,
            val name: String = "",
            val price: String = "",
            val slots: Int = 0,
            val visible: Int = 0
        )
    }
}