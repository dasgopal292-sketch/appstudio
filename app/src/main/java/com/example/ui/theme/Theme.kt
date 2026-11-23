package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val StudioPixyColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = ObsidianBlack,
    primaryContainer = GoldContainer,
    onPrimaryContainer = BrightGold,
    secondary = BrightGold,
    onSecondary = ObsidianBlack,
    secondaryContainer = ElevatedCard,
    onSecondaryContainer = IvoryText,
    tertiary = BronzeGold,
    onTertiary = IvoryText,
    background = ObsidianBlack,
    onBackground = IvoryText,
    surface = CharcoalSurface,
    onSurface = IvoryText,
    surfaceVariant = ElevatedCard,
    onSurfaceVariant = MutedStone,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
    error = CrimsonError,
    onError = IvoryText
)

private val StudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun StudioPixyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioPixyColorScheme,
        typography = StudioTypography,
        shapes = StudioShapes,
        content = content
    )
}
