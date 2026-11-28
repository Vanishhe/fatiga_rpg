package ru.college.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import ru.college.app.ui.theme.AccentMutedRed
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import kotlin.math.ceil

/**
 * Segmented HP bar from the Figma mock: filled cells use the muted red,
 * empty cells use the background black, all inside a 1dp frame.
 */
@Composable
fun PixelHpBar(
    hp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier,
    segments: Int = 12
) {
    val ratio = if (maxHp <= 0) 0f else hp.toFloat() / maxHp.toFloat()
    val filled = ceil(ratio * segments).toInt().coerceIn(0, segments)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val border = 1.dp.toPx()
        val gap = 2.dp.toPx()
        drawRect(color = PanelDarkGray)

        val innerWidth = (size.width - border * 2).coerceAtLeast(0f)
        val innerHeight = (size.height - border * 2).coerceAtLeast(0f)
        val cellWidth = ((innerWidth - gap * (segments - 1)) / segments).coerceAtLeast(0f)

        repeat(segments) { index ->
            drawRect(
                color = if (index < filled) AccentMutedRed else BgBlack,
                topLeft = Offset(
                    x = border + index * (cellWidth + gap),
                    y = border
                ),
                size = Size(cellWidth, innerHeight)
            )
        }
    }
}
