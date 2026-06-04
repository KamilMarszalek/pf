package ui

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartUtilsTest {

    @Test
    fun `should split offsets into non-null segments`() {
        val result = segmentOffsets(
            listOf(
                Offset(1f, 1f),
                Offset(2f, 2f),
                null,
                null,
                Offset(3f, 3f),
                null,
                Offset(4f, 4f),
            )
        )

        assertEquals(
            listOf(
                listOf(Offset(1f, 1f), Offset(2f, 2f)),
                listOf(Offset(3f, 3f)),
                listOf(Offset(4f, 4f)),
            ),
            result,
        )
    }

    @Test
    fun `initial visible range should prefer the latest candles`() {
        assertEquals(50 until 150, initialVisibleRange(totalCount = 150, preferredCount = 100))
    }

    @Test
    fun `initial visible range should clamp preferred count to total count`() {
        assertEquals(0 until 50, initialVisibleRange(totalCount = 50, preferredCount = 100))
    }

    @Test
    fun `initial visible range should handle non positive total count`() {
        assertEquals(0..0, initialVisibleRange(totalCount = 0, preferredCount = 100))
        assertEquals(0..0, initialVisibleRange(totalCount = -10, preferredCount = 100))
    }

    @Test
    fun `pan visible range should stay inside available data`() {
        assertEquals(20 until 30, panVisibleRange(range = 10 until 20, totalCount = 100, shift = 10))
        assertEquals(0 until 10, panVisibleRange(range = 10 until 20, totalCount = 100, shift = -100))
        assertEquals(90 until 100, panVisibleRange(range = 80 until 90, totalCount = 100, shift = 100))
    }

    @Test
    fun `pan visible range should handle empty range or non positive total count`() {
        assertEquals(0..0, panVisibleRange(range = IntRange.EMPTY, totalCount = 100, shift = 10))
        assertEquals(0..0, panVisibleRange(range = 0 until 10, totalCount = 0, shift = 10))
        assertEquals(0..0, panVisibleRange(range = 0 until 10, totalCount = -1, shift = 10))
    }

    @Test
    fun `zoom visible range should keep center and clamp count`() {
        assertEquals(40 until 60, zoomVisibleRange(range = 30 until 70, totalCount = 100, zoomFactor = 0.5))
        assertEquals(10 until 90, zoomVisibleRange(range = 30 until 70, totalCount = 100, zoomFactor = 2.0))
    }

    @Test
    fun `zoom visible range should handle datasets smaller than minimum visible count`() {
        assertEquals(0 until 5, zoomVisibleRange(range = 0 until 5, totalCount = 5, zoomFactor = 0.5))
        assertEquals(0 until 5, zoomVisibleRange(range = 0 until 5, totalCount = 5, zoomFactor = 2.0))
    }
}

