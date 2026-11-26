package ru.college.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The game is always dark: no dynamic colour, no light scheme.
private val FatigaColorScheme = darkColorScheme(
    primary = AccentOffWhite,
    onPrimary = BgBlack,
    primaryContainer = NightIndigo,
    onPrimaryContainer = AccentOffWhite,
    secondary = AccentMutedBlue,
    onSecondary = AccentOffWhite,
    tertiary = AccentMutedRed,
    onTertiary = AccentOffWhite,
    background = BgBlack,
    onBackground = AccentOffWhite,
    surface = BgBlack,
    onSurface = AccentOffWhite,
    surfaceVariant = NightIndigo,
    onSurfaceVariant = TextLightGray,
    outline = PanelDarkGray,
    outlineVariant = AccentLeather,
    error = AccentMutedRed,
    onError = AccentOffWhite
)

@Composable
fun FatigaTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FatigaColorScheme,
        typography = Typography,
        content = content
    )
}
