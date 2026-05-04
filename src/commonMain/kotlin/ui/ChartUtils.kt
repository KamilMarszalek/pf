package ui


import androidx.compose.ui.geometry.Offset

fun segmentOffsets(offsets: List<Offset?>): List<List<Offset>> =
    offsets
        .fold(emptyList<List<Offset>>() to emptyList<Offset>()) { (segments, current), point ->
            if (point != null) segments to (current + point)
            else (if (current.isNotEmpty()) segments + listOf(current) else segments) to emptyList()
        }
        .let { (segments, last) ->
            if (last.isNotEmpty()) segments + listOf(last) else segments
        }