package ru.college.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.PixelUltima
import ru.college.app.ui.theme.TextLightGray

/**
 * Square, flat button from the Figma reference: one solid fill, no shadows,
 * optional pixel chevron on the right.
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AccentOffWhite,
    contentColor: Color = BgBlack,
    trailingSymbol: String? = null
) {
    val fill = if (enabled) containerColor else PanelDarkGray
    val content = if (enabled) contentColor else TextLightGray

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(fill, RoundedCornerShape(0.dp))
            .border(1.dp, fill, RoundedCornerShape(0.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = text,
                color = content,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = PixelUltima,
                textAlign = TextAlign.Center
            )
            if (trailingSymbol != null) {
                Text(
                    text = trailingSymbol,
                    color = content,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = PixelUltima
                )
            }
        }
    }
}
