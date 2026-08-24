package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WarehouseNavyLight,
    onPrimary = WarehouseNavyDark,
    primaryContainer = WarehouseNavy,
    onPrimaryContainer = Color(0xFFDCE8FE),
    secondary = WarehouseSteelBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = WarehouseAccentAmber,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = WarehouseNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = WarehouseNavyDark,
    secondary = WarehouseSteelBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0EDFB),
    onSecondaryContainer = WarehouseNavyDark,
    tertiary = WarehouseAccentAmber,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional warehouse palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
