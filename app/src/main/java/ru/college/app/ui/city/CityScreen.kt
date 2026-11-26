package ru.college.app.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.college.app.R
import ru.college.app.ui.components.PixelButton
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.TextLightGray

/**
 * Screen 3 — temporary city stub. Shrine / Armory / Portal come next.
 */
@Composable
fun CityScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.city_online_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGray
                )
                Text(
                    text = stringResource(R.string.city_screen_index),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGray
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.city_title),
                style = MaterialTheme.typography.headlineLarge,
                color = AccentOffWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.city_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TextLightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.city_placeholder_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLightGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PixelButton(
                text = stringResource(R.string.city_back),
                onClick = onBack
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.main_footer),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
