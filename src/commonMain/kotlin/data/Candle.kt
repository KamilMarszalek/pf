package data

import kotlinx.serialization.Serializable

@Serializable
data class Candle (
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
)