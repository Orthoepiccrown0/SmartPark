package com.epiccrown.smartpark.model.response

data class HomeResponse(
    val activeCars: List<ActiveCar> = listOf(),
    val availablePayments: List<AvailablePayment> = listOf(),
    val lastTransactions: List<LastTransaction> = listOf(),
    val otherInfo: OtherInfo = OtherInfo(),
    val spentThisMonth: String = "0.0",
    val totalSpent: String = "0.0"
) {
    data class ActiveCar(
        val enterTimestamp: String = "",
        val parkName: String = "",
        val parkPrice: String = "",
        val plate: String = ""
    )

    data class AvailablePayment(
        val carDateIn: String = "",
        val carDateOut: String = "",
        val `import`: String = "0",
        val parkName: String = "",
        val plate: String = ""
    )

    data class LastTransaction(
        val carDateIn: String = "",
        val carDateOut: String = "",
        val `import`: String = "",
        val parkName: String = "",
        val plate: String = ""
    )

    data class OtherInfo(
        val debit: Double = 0.0,
        val numCars: String = "0",
        val numParkedTimes: String = "0"
    )
}