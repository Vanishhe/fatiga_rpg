package ru.college.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource

/**
 * Pixel-art sprite. FilterQuality.None is mandatory so Android never blurs it.
 */
@Composable
fun PixelSprite(
    @DrawableRes drawableRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    flipHorizontal: Boolean = false
) {
    Image(
        bitmap = ImageBitmap.imageResource(drawableRes),
        contentDescription = contentDescription,
        modifier = modifier.graphicsLayer(
            scaleX = if (flipHorizontal) -1f else 1f
        ),
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.None
    )
}
