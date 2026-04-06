package com.example.studypartner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = Indigo600,
    onPrimary          = Color.White,
    primaryContainer   = Indigo100,
    onPrimaryContainer = Indigo800,
    secondary          = Teal500,
    onSecondary        = Color.White,
    secondaryContainer = Teal200,
    onSecondaryContainer = Color(0xFF00201E),
    background         = BackgroundLight,
    onBackground       = Color(0xFF1A1C2A),
    surface            = SurfaceLight,
    onSurface          = Color(0xFF1A1C2A),
    surfaceVariant     = SurfaceVariantL,
    onSurfaceVariant   = Color(0xFF44475A),
)

private val DarkColorScheme = darkColorScheme(
    primary            = Indigo200,
    onPrimary          = Color(0xFF1A237E),
    primaryContainer   = Indigo800,
    onPrimaryContainer = Indigo100,
    secondary          = Teal200Dark,
    onSecondary        = Color(0xFF003731),
    background         = BackgroundDark,
    onBackground       = Color(0xFFE2E3F0),
    surface            = SurfaceDark,
    onSurface          = Color(0xFFE2E3F0),
    surfaceVariant     = Color(0xFF272A38),
    onSurfaceVariant   = Color(0xFFB0B3C6),
)

@Composable
fun StudyPartnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
