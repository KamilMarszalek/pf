package ui

import androidx.compose.ui.geometry.Offset

data class DrawingPoint(
    val candleIndex: Int,
    val price: Double,
)

data class DrawingLineState(
    val firstPoint: DrawingPoint? = null,
    val currentTouchPos: Offset? = null,
)

