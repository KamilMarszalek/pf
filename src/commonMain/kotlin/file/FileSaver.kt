package file


sealed class SaveFileResult {
    data class Success(val path: String): SaveFileResult()
    data object Cancelled: SaveFileResult()
    data class Failure(val message: String): SaveFileResult()
}

expect fun saveTextFile(
    suggestedFileName: String,
    content: String,
) : SaveFileResult