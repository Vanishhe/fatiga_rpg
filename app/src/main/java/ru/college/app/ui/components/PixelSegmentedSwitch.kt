package ru.college.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.PixelUltima
import ru.college.app.ui.theme.TextLightGray

/**
 * Two-state tab switch (LOGIN / REGISTER) with hard corners and no animation.
 */
@Composable
fun PixelSegmentedSwitch(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(if (selected) AccentOffWhite else BgBlack)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = PixelUltima,
                    color = if (selected) BgBlack else TextLightGray
                )
            }
        }
    }
}
