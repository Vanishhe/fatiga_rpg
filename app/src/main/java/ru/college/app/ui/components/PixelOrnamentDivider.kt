package ru.college.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import ru.college.app.ui.theme.AccentMutedBlue
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.PanelDarkGray

/**
 * Horizontal rule broken by a small pixel "sword" ornament, as on the mock.
 * Drawn on a Canvas so no extra vector asset is needed.
 */
@Composable
fun PixelOrnamentDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            val y = size.height * 0.62f
            drawRect(
                color = PanelDarkGray,
                topLeft = Offset(0f, y),
                size = Size(size.width, 1.dp.toPx())
            )
        }

        Canvas(
            modifier = Modifier
                .width(40.dp)
                .height(48.dp)
        ) {
            // Blade.
            drawRect(
                color = AccentOffWhite,
                topLeft = Offset(size.width / 2f - 1.dp.toPx(), 4.dp.toPx()),
                size = Size(2.dp.toPx(), 20.dp.toPx())
            )
            // Cross-guard.
            drawRect(
                color = AccentMutedBlue,
                topLeft = Offset(6.dp.toPx(), 24.dp.toPx()),
                size = Size((size.width - 12.dp.toPx()), 4.dp.toPx())
            )
            // Grip.
            drawRect(
                color = PanelDarkGray,
                topLeft = Offset(size.width / 2f - 2.dp.toPx(), 28.dp.toPx()),
                size = Size(4.dp.toPx(), 10.dp.toPx())
            )
            // Pommel.
            drawRect(
                color = AccentMutedBlue,
                topLeft = Offset(size.width / 2f - 4.dp.toPx(), 38.dp.toPx()),
                size = Size(8.dp.toPx(), 3.dp.toPx())
            )
        }

        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            val y = size.height * 0.62f
            drawRect(
                color = PanelDarkGray,
                topLeft = Offset(0f, y),
                size = Size(size.width, 1.dp.toPx())
            )
        }
    }
}
