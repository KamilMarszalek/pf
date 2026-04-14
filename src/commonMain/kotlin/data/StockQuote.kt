package data

import kotlinx.serialization.Serializable

@Serializable
data class StockQuote(
    val symbol: String,
    val price: Float,
    val changePercentage: Float,
    val exchange: String
)
