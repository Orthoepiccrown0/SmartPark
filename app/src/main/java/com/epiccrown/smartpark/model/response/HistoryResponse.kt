package com.epiccrown.smartpark.model.response

data class HistoryResponse(
    val failedPayments: List<Payment> = listOf(),
    val pendingPayments: List<Payment> = listOf(),
    val succeedPayments: List<Payment> = listOf()
) {
    data class Payment(
        val carDateIn: String = "",
        val carDateOut: String = "",
        val `import`: String = "",
        val parkName: String = "",
        val plate: String = ""
    )


}