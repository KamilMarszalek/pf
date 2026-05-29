package ui


import androidx.compose.ui.geometry.Offset
import kotlin.math.roundToInt

const val CHART_PADDING_PX = 30f

fun segmentOffsets(offsets: List<Offset?>): List<List<Offset>> =
    offsets
        .fold(emptyList<List<Offset>>() to emptyList<Offset>()) { (segments, current), point ->
            if (point != null) segments to (current + point)
            else (if (current.isNotEmpty()) segments + listOf(current) else segments) to emptyList()
        }
        .let { (segments, last) ->
            if (last.isNotEmpty()) segments + listOf(last) else segments
        }

fun initialVisibleRange(
    totalCount: Int,
    preferredCount: Int = 100,
): IntRange {
    if (totalCount <= 0) return 0..0

    val count = preferredCount.coerceIn(1, totalCount)
    return (totalCount - count) until totalCount
}

fun panVisibleRange(
    range: IntRange,
    totalCount: Int,
    shift: Int,
): IntRange {
    if (totalCount <= 0 || range.isEmpty()) return 0..0

    val count = range.count()
    val maxFirst = (totalCount - count).coerceAtLeast(0)

    val newFirst = (range.first + shift).coerceIn(0, maxFirst)

    return newFirst until newFirst + count
}

fun zoomVisibleRange(
    range: IntRange,
    totalCount: Int,
    zoomFactor: Double,
    minVisibleCount: Int = 20,
): IntRange {
    if (totalCount <= 0 || range.isEmpty()) return 0..0

    val currentCount = range.count()
    val minCount = minOf(minVisibleCount, totalCount).coerceAtLeast(1)

    val newCount = (currentCount * zoomFactor)
        .roundToInt()
        .coerceIn(minCount, totalCount)

    val center = range.first + currentCount / 2
    val maxFirst = (totalCount - newCount).coerceAtLeast(0)

    val newFirst = (center - newCount / 2).coerceIn(0, maxFirst)

    return newFirst until newFirst + newCount
}
