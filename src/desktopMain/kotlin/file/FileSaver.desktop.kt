package file

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual fun saveTextFile(
    suggestedFileName: String,
    content: String,
): SaveFileResult {
    return try {
        val dialog = FileDialog(null as Frame?, "Save CSV", FileDialog.SAVE).apply {
            file = suggestedFileName
            isVisible = true
        }

        val directory = dialog.directory
        val file = dialog.file

        if (directory == null || file == null) {
            return SaveFileResult.Cancelled
        }
        val target = File(directory, file)
        target.writeText(content)
        SaveFileResult.Success(target.absolutePath)
    } catch (e: Exception) {
        SaveFileResult.Failure(e.message ?: "Unknown error while saving file")
    }
}