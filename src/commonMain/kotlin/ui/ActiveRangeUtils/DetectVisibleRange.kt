package ui.ActiveRangeUtils

fun detectVisibleRange(
    range: IntRange,
    marks: VisibleRangeMarks
): VisibleRange {
    if (range.last != marks.fullSize) {
        return VisibleRange.NONE
    }

    return when(range.first) {
        0 -> VisibleRange.FIVE_YEAR
        (marks.oneYearMark) -> VisibleRange.ONE_YEAR
        (marks.sixMonthsMark) -> VisibleRange.SIX_MONTHS
        (marks.threeMonthsMark) -> VisibleRange.THREE_MONTHS
        (marks.oneMonthMark) -> VisibleRange.ONE_MONTH
        else -> VisibleRange.NONE
    }
}
