package com.startspeler.horeca.ui.theme.crew

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CrewColorScheme = lightColorScheme(
    primary = CrewPrimary,
    onPrimary = CrewOnPrimary,

    secondary = CrewAccent,
    onSecondary = CrewOnAccent,

    tertiary = SuccessGreen,
    onTertiary = CrewOnPrimary,

    background = CrewBackground,
    onBackground = CrewTextPrimary,

    surface = CrewSurface,
    onSurface = CrewTextPrimary,

    surfaceVariant = CrewSurfaceVariant,
    onSurfaceVariant = CrewTextSecondary,

    error = ErrorRed,
    onError = CrewOnPrimary,

    outline = CrewBorder,
    outlineVariant = CrewDivider,
)

@Composable
fun StartSpelerCrewTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CrewColorScheme,
        content = content
    )
}