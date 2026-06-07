package com.startspeler.horeca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StartSpelerColorScheme = darkColorScheme(
    primary = BrandGreen,
    onPrimary = BackgroundDark,

    secondary = BrandBlue,
    onSecondary = TextWhite,

    tertiary = SuccessGreen,
    onTertiary = BackgroundDark,

    background = BackgroundDark,
    onBackground = TextWhite,

    surface = SurfaceDark,
    onSurface = TextWhite,

    surfaceVariant = CardDark,
    onSurfaceVariant = TextGray,

    error = ErrorRed,
    onError = TextWhite,

    outline = BorderDark,
    outlineVariant = DividerDark
)

@Composable
fun StartSpelerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StartSpelerColorScheme,
        content = content
    )
}