package com.mani.carcassonnescorekeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.data.PawnPalette

// Field green, road brown and roof red on parchment.
private val LightColors = lightColorScheme(
    primary = Color(0xFF4E6E2A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFE8AE),
    onPrimaryContainer = Color(0xFF13200A),
    secondary = Color(0xFF8A5A2B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5DCC2),
    onSecondaryContainer = Color(0xFF2F1500),
    tertiary = Color(0xFFB5462F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD2),
    onTertiaryContainer = Color(0xFF3D0600),
    background = Color(0xFFFBF7EE),
    onBackground = Color(0xFF1E1C16),
    surface = Color(0xFFFBF7EE),
    onSurface = Color(0xFF1E1C16),
    surfaceVariant = Color(0xFFE3DCCB),
    onSurfaceVariant = Color(0xFF4A463B),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F1E4),
    surfaceContainer = Color(0xFFF1EADC),
    surfaceContainerHigh = Color(0xFFEBE4D5),
    surfaceContainerHighest = Color(0xFFE5DECF),
    outline = Color(0xFF7B7667),
    outlineVariant = Color(0xFFCBC5B5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB3D18F),
    onPrimary = Color(0xFF203708),
    primaryContainer = Color(0xFF36531A),
    onPrimaryContainer = Color(0xFFCFE8AE),
    secondary = Color(0xFFE8BD92),
    onSecondary = Color(0xFF472A0A),
    secondaryContainer = Color(0xFF61401F),
    onSecondaryContainer = Color(0xFFF5DCC2),
    tertiary = Color(0xFFFFB4A3),
    onTertiary = Color(0xFF5F1507),
    tertiaryContainer = Color(0xFF8F2E1B),
    onTertiaryContainer = Color(0xFFFFDAD2),
    background = Color(0xFF17150F),
    onBackground = Color(0xFFE9E2D4),
    surface = Color(0xFF17150F),
    onSurface = Color(0xFFE9E2D4),
    surfaceVariant = Color(0xFF4A463B),
    onSurfaceVariant = Color(0xFFCDC6B4),
    surfaceContainerLowest = Color(0xFF12100B),
    surfaceContainerLow = Color(0xFF1F1C16),
    surfaceContainer = Color(0xFF23201A),
    surfaceContainerHigh = Color(0xFF2D2A23),
    surfaceContainerHighest = Color(0xFF38352D),
    outline = Color(0xFF969080),
    outlineVariant = Color(0xFF4A463B),
)

/** Colour used for "points added" and "points deducted" in logs. */
val GainColor @Composable get() = if (isSystemInDarkTheme()) Color(0xFF8FD18A) else Color(0xFF2E7D32)

val GoldColor = Color(0xFFE0A800)
val SilverColor = Color(0xFF9EA4AA)
val BronzeColor = Color(0xFFB87333)

private val presetNames = mapOf(
    PawnPalette.YELLOW to R.string.color_yellow,
    PawnPalette.GREEN to R.string.color_green,
    PawnPalette.BLUE to R.string.color_blue,
    PawnPalette.BLACK to R.string.color_black,
    PawnPalette.RED to R.string.color_red,
    PawnPalette.PINK to R.string.color_pink,
)

/** "Yellow" for a preset, otherwise "Custom colour #12AB34". */
@Composable
fun pawnColorName(argb: Int): String =
    presetNames[argb]?.let { stringResource(it) }
        ?: stringResource(R.string.color_custom_hex, PawnPalette.toHex(argb))

/** Black or white, whichever reads better on a pawn of colour [argb]. */
fun onPawnColor(argb: Int): Color =
    if (Color(argb).luminance() > 0.45f) Color(0xFF1E1C16) else Color.White

@Composable
fun CarcassonneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
