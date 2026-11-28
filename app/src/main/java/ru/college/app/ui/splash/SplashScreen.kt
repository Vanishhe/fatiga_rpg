package ru.college.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
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
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.TextLightGray

/**
 * Shown while the saved session is validated against Room. Prevents the login
 * screen from flashing for users who are already signed in.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBlack)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.displayLarge,
                color = AccentOffWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
