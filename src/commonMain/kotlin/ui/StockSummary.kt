package ui

import analysis.StockAnalysis
import analysis.calculateStockStatistics
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun StockSummary(
    analysis: StockAnalysis,
    modifier: Modifier = Modifier,
) {
    val statistics = remember(analysis.candles) {
        calculateStockStatistics(analysis.candles)
    } ?: return

    val latestClose = analysis.candles.lastOrNull()?.close ?: return
    val latestSma = analysis.sma.lastNonNullValue()
    val latestEma = analysis.ema.lastNonNullValue()
    val latestRsi = analysis.rsi.lastNonNullValue()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        SummaryRow(
            "Latest close: ${formatNumber(latestClose)}",
            "Return: ${formatPercent(statistics.totalReturnPercent)}",
            "Volatility: ${formatPercent(statistics.volatilityPercent)}"
        )
        SummaryRow(
            "Min close: ${formatNumber(statistics.minClose)}",
            "Max close: ${formatNumber(statistics.maxClose)}",
            "Avg close: ${formatNumber(statistics.averageClose)}",
            "Avg volume: ${formatNumber(statistics.averageVolume)}"
        )
        SummaryRow(
            "SMA${analysis.smaPeriod}: ${formatOptionalNumber(latestSma)}",
            "EMA${analysis.emaPeriod}: ${formatOptionalNumber(latestEma)}",
            "RSI${analysis.rsiPeriod}: ${formatOptionalNumber(latestRsi)} (${interpretRsi(latestRsi)})"
        )
    }
}

@Composable
private fun SummaryRow(vararg values: String) {
    Row(modifier = Modifier.padding(top = 2.dp)) {
        values.forEachIndexed { index, value ->
            if (index > 0) Spacer(Modifier.width(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.caption,
                color = Color.DarkGray,
            )
        }
    }
}

private fun List<Double?>.lastNonNullValue(): Double? =
    lastOrNull { it != null }

private fun interpretRsi(value: Double?): String =
    when {
        value == null -> "not available"
        value < 30.0 -> "oversold"
        value > 70.0 -> "overbought"
        else -> "neutral"
    }

private fun formatOptionalNumber(value: Double?): String =
    value?.let(::formatNumber) ?: "-"

private fun formatPercent(value: Double): String =
    "${formatNumber(value)}%"

private fun formatNumber(value: Double): String =
    String.format(Locale.US, "%.2f", value)
