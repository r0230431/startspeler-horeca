package com.startspeler.horeca.ui.theme.customer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CustomerColorScheme = darkColorScheme(
    primary = CustomerPrimary,
    onPrimary = CustomerOnPrimary,

    secondary = CustomerSecondary,
    onSecondary = CustomerOnSecondary,

    tertiary = CustomerSuccess,
    onTertiary = CustomerOnPrimary,

    background = CustomerBackground,
    onBackground = CustomerOnBackground,

    surface = CustomerSurface,
    onSurface = CustomerOnSurface,

    surfaceVariant = CustomerSurfaceVariant,
    onSurfaceVariant = CustomerOnSurfaceVariant,

    error = CustomerError,
    onError = CustomerOnError,

    outline = CustomerOutline,
    outlineVariant = CustomerOutlineVariant
)

@Composable
fun StartSpelerCustomerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomerColorScheme,
        content = content
    )
}