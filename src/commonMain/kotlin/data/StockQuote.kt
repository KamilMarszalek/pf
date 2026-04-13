package data

import kotlinx.serialization.Serializable

@Serializable
data class StockQuote(
    val symbol: String,
    val price: Float,
    val changesPercentage: Float,
    val exchange: String
)
