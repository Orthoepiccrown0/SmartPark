package com.epiccrown.smartpark.model

data class ProcessDataResponse(
    val data_type: String = "",
    val epoch_time: Long = 0,
    val img_height: Int = 0,
    val img_width: Int = 0,
    val processing_time_ms: Double = 0.0,
    val regions_of_interest: List<RegionsOfInterest> = listOf(),
    val results: List<Result> = listOf(),
    val version: Int = 0
) {
    data class RegionsOfInterest(
        val height: Int = 0,
        val width: Int = 0,
        val x: Int = 0,
        val y: Int = 0
    )

    data class Result(
        val candidates: List<Candidate> = listOf(),
        val confidence: Double = 0.0,
        val coordinates: List<Coordinate> = listOf(),
        val matches_template: Int = 0,
        val plate: String = "",
        val plate_index: Int = 0,
        val processing_time_ms: Double = 0.0,
        val region: String = "",
        val region_confidence: Int = 0,
        val requested_topn: Int = 0
    ) {
        data class Candidate(
            val confidence: Double = 0.0,
            val matches_template: Int = 0,
            val plate: String = ""
        )

        data class Coordinate(
            val x: Float = 0f,
            val y: Float = 0f
        )
    }
}