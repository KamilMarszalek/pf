package data
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
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

// Immutable helper to store client
class StockProvider(
    private val client: HttpClient,
) {
    private val baseUrl = "https://financialmodelingprep.com/api/v3"

    // Higher-order function
    suspend fun fetchQuote(symbol: String): ApiResult<StockQuote> = try {
        val response = client.get("$baseUrl/quote/$symbol?apikey=${AppConfig.API_KEY}")
        val quotes = response.body<List<StockQuote>>()

        quotes.firstOrNull()?.let { ApiResult.Success(it) }
            ?: ApiResult.Failure("Symbol $symbol not found")
    } catch (e: Exception) {
        ApiResult.Failure(e.message ?: "Unknown error")
    }
}