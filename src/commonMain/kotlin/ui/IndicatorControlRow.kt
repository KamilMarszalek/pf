package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IndicatorControlRow(
    label: String,
    period: Int,
    onPeriodChange: (Int) -> Unit,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    badgeColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        badgeColor?.let { Box(modifier = Modifier.size(12.dp).background(it)) }

        Text(
            text = " $label$period",
            style = MaterialTheme.typography.caption,
            modifier = Modifier.width(50.dp)
        )
        IconButton(onClick = { onVisibilityChange(!isVisible) }, modifier = Modifier.size(20.dp)) {
            Icon(
                imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle $label",
                modifier = Modifier.size(14.dp)
            )
        }
        Slider(
            value = period.toFloat(),
            onValueChange = { onPeriodChange(it.toInt()) },
            valueRange = valueRange,
            modifier = Modifier.width(120.dp).height(24.dp)
        )
    }
}