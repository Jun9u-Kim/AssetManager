package com.lgtm.asset_manager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AssetColorScheme = lightColorScheme(
    primary = AssetRed,
    onPrimary = Color.White,
    primaryContainer = AssetRedContainer,
    onPrimaryContainer = AssetInk,
    secondary = AssetInk,
    onSecondary = Color.White,
    tertiary = Color(0xFF9F3030),
    background = AssetBackground,
    onBackground = AssetInk,
    surface = AssetSurface,
    onSurface = AssetInk,
    surfaceVariant = AssetSurfaceVariant,
    onSurfaceVariant = AssetMutedInk,
    outline = AssetOutline,
    outlineVariant = Color(0xFFE2E2E2),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun Asset_ManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AssetColorScheme,
        typography = Typography,
        content = content,
    )
}
