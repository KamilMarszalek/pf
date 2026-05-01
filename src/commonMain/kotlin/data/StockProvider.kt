package data
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            coerceInputValues = true
        })
    }
}

// Error handling
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val message: String) : ApiResult<Nothing>()
}

@Serializable
data class FmpError(
    @SerialName("Error Message") val message: String
)

// Immutable helper to store client
class StockProvider(
    private val client: HttpClient,
    private val apiKey: String,
) {
    private val baseUrl = "https://financialmodelingprep.com/stable"
    private val json  = Json {ignoreUnknownKeys = true}

    // Higher-order function
    suspend fun fetchQuote(symbol: String): ApiResult<StockQuote> = try {
        val response = client.get(baseUrl) {
            url {
                appendPathSegments("profile")
                parameters.append("symbol", symbol)
                parameters.append("apikey", apiKey)
            }
            println(url)
        }
        val bodyString = response.bodyAsText()



        if (response.status.value == 200) {
            val quotes = json.decodeFromString<List<StockQuote>>(bodyString)
            quotes.firstOrNull()?.let { ApiResult.Success(it) }
                ?: ApiResult.Failure("Symbol $symbol not found")
        } else {
            val errorBody = runCatching { json.decodeFromString<FmpError>(bodyString) }.getOrNull()
            ApiResult.Failure(errorBody?.message ?: "Server error ${response.status}")
        }
    } catch (e: Exception) {
        ApiResult.Failure(e.message ?: "Unknown error")
    }

    suspend fun fetchCandles(symbol: String): ApiResult<List<Candle>> = try {
        val response = client.get(baseUrl) {
            url {
                appendPathSegments("historical-price-eod", "full")
                parameters.append("symbol", symbol)
                parameters.append("apikey", apiKey)
            }
        }
        val bodyString = response.bodyAsText()

        if (response.status.value == 200) {
            val candles = json.decodeFromString<List<Candle>>(bodyString)
            ApiResult.Success(candles)
        } else {
            ApiResult.Failure("Server error ${response.status}")
        }
    } catch (e: Exception) {
        ApiResult.Failure(e.message ?: "Unknown error")
    }
}