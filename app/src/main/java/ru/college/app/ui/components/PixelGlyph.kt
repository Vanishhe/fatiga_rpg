package ru.college.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import ru.college.app.ui.theme.AccentMutedBlue
import ru.college.app.ui.theme.AccentMutedRed
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.PanelDarkGray

/**
 * Tiny programmatic pixel icons (no extra drawables, no blurry scaling).
 * Each character maps to a palette colour; '.' stays transparent.
 */
enum class GlyphArt { SHRINE, SWORD, PORTAL, POTION_RED, POTION_BLUE }

@Composable
fun PixelGlyph(
    art: GlyphArt,
    modifier: Modifier = Modifier
) {
    val pattern = when (art) {
        GlyphArt.SHRINE -> SHRINE
        GlyphArt.SWORD -> SWORD
        GlyphArt.PORTAL -> PORTAL
        GlyphArt.POTION_RED -> POTION
        GlyphArt.POTION_BLUE -> POTION.map { row -> row.replace('r', 'b') }
    }

    Canvas(modifier = modifier) {
        val columns = pattern.maxOf { it.length }
        val rows = pattern.size
        if (columns == 0 || rows == 0) return@Canvas

        val cellWidth = size.width / columns
        val cellHeight = size.height / rows

        pattern.forEachIndexed { y, row ->
            row.forEachIndexed { x, symbol ->
                val color = symbol.toColor() ?: return@forEachIndexed
                drawRect(
                    color = color,
                    topLeft = Offset(x * cellWidth, y * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
    }
}

private fun Char.toColor(): Color? = when (this) {
    'w' -> AccentOffWhite
    'g' -> PanelDarkGray
    'r' -> AccentMutedRed
    'b' -> AccentMutedBlue
    else -> null
}

private val SHRINE = listOf(
    "................",
    ".......ww.......",
    "......wrrw......",
    "......wrrw......",
    ".......ww.......",
    "................",
    "...gggggggggg...",
    "...gwwwwwwwwg...",
    "...gwwwwwwwwg...",
    "...gwwwwwwwwg...",
    "...gggggggggg...",
    "....g......g....",
    "...gg......gg...",
    "..gggggggggggg..",
    "................",
    "................"
)

private val SWORD = listOf(
    "................",
    ".......ww.......",
    ".......ww.......",
    ".......ww.......",
    ".......ww.......",
    ".......ww.......",
    ".......ww.......",
    ".......ww.......",
    ".......ww.......",
    "....wwwwwwww....",
    ".......gg.......",
    ".......gg.......",
    ".......gg.......",
    ".......rr.......",
    "................",
    "................"
)

private val PORTAL = listOf(
    "................",
    "....gggggggg....",
    "...gwwwwwwwwg...",
    "..gwwwwwwwwwwg..",
    "..gwwbbwwbbwwg..",
    "..gwwbbwwbbwwg..",
    "..gwwwwwwwwwwg..",
    "..gwwwwwwwwwwg..",
    "..gwwwwwwwwwwg..",
    "..gwwwwwwwwwwg..",
    "..gwwwwwwwwwwg..",
    "..gwwwwwwwwwwg..",
    "..gwwwwwwwwwwg..",
    "..gggggggggggg..",
    "................",
    "................"
)

private val POTION = listOf(
    "................",
    "......gggg......",
    "......g..g......",
    "......g..g......",
    ".....gggggg.....",
    "....gwwwwwwg....",
    "...gwwrrrrwwg...",
    "...gwwrrrrwwg...",
    "...gwwrrrrwwg...",
    "...gwwrrrrwwg...",
    "...gwwwwwwwwg...",
    "....gwwwwwwg....",
    ".....gggggg.....",
    "................",
    "................",
    "................"
)
