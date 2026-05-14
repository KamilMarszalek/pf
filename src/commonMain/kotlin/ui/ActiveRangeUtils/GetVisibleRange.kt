package ui.ActiveRangeUtils

fun getVisibleRange(
    visibleRange: VisibleRange,
    marks: VisibleRangeMarks,
) : IntRange{
    return when(visibleRange) {
        VisibleRange.NONE -> IntRange(0, marks.fullSize)
        VisibleRange.FIVE_YEAR -> IntRange(0, marks.fullSize)
        VisibleRange.ONE_YEAR -> IntRange(marks.oneYearMark, marks.fullSize)
        VisibleRange.SIX_MONTHS -> IntRange(marks.sixMonthsMark, marks.fullSize)
        VisibleRange.THREE_MONTHS -> IntRange(marks.threeMonthsMark, marks.fullSize)
        VisibleRange.ONE_MONTH -> IntRange(marks.oneMonthMark, marks.fullSize)
    }
}