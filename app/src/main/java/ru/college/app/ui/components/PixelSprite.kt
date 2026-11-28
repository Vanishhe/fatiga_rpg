package ru.college.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource

/**
 * Pixel-art sprite. FilterQuality.None is mandatory so Android never blurs it.
 * [tint] is used by the battle screen for the short damage flash.
 */
@Composable
fun PixelSprite(
    @DrawableRes drawableRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    flipHorizontal: Boolean = false,
    tint: Color? = null
) {
    Image(
        bitmap = ImageBitmap.imageResource(drawableRes),
        contentDescription = contentDescription,
        modifier = modifier.graphicsLayer(
            scaleX = if (flipHorizontal) -1f else 1f
        ),
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.None,
        colorFilter = tint?.let { color -> ColorFilter.tint(color) }
    )
}
