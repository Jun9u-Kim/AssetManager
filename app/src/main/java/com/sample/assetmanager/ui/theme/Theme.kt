package com.sample.assetmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FinanceColorScheme = lightColorScheme(
    primary = FinanceRed,
    onPrimary = Color.White,
    primaryContainer = FinanceRedContainer,
    onPrimaryContainer = FinanceInk,
    secondary = FinanceInk,
    onSecondary = Color.White,
    tertiary = Color(0xFF9F3030),
    background = FinanceBackground,
    onBackground = FinanceInk,
    surface = FinanceSurface,
    onSurface = FinanceInk,
    surfaceVariant = FinanceSurfaceVariant,
    onSurfaceVariant = FinanceMutedInk,
    outline = FinanceOutline,
    outlineVariant = Color(0xFFE2E2E2),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun AssetManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FinanceColorScheme,
        typography = Typography,
        content = content,
    )
}
