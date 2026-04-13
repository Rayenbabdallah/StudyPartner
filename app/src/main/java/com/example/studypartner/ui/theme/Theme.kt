package com.example.studypartner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Crextio Modernist — "Digital Atelier" brand palette.
// Dynamic color is intentionally disabled: this is a curated warm-amber identity.

private val LightColorScheme = lightColorScheme(
    primary                = StitchPrimary,
    onPrimary              = StitchOnPrimary,
    primaryContainer       = StitchPrimaryContainer,
    onPrimaryContainer     = StitchOnPrimaryContainer,

    secondary              = StitchSecondary,
    onSecondary            = StitchOnSecondary,
    secondaryContainer     = StitchSecondaryContainer,
    onSecondaryContainer   = StitchOnSecondaryContainer,

    tertiary               = StitchTertiary,
    onTertiary             = StitchOnTertiary,
    tertiaryContainer      = StitchTertiaryContainer,
    onTertiaryContainer    = StitchOnTertiaryContainer,

    background             = StitchBackground,
    onBackground           = StitchOnBackground,

    surface                = StitchSurface,
    onSurface              = StitchOnSurface,
    surfaceVariant         = StitchSurfaceVariant,
    onSurfaceVariant       = StitchOnSurfaceVariant,
    surfaceContainerLowest = StitchSurfaceContainerLowest,
    surfaceContainerLow    = StitchSurfaceContainerLow,
    surfaceContainer       = StitchSurfaceContainer,
    surfaceContainerHigh   = StitchSurfaceContainerHigh,
    surfaceContainerHighest= StitchSurfaceContainerHighest,
    inverseSurface         = StitchInverseSurface,
    inverseOnSurface       = StitchInverseOnSurface,
    inversePrimary         = StitchInversePrimary,

    outline                = StitchOutline,
    outlineVariant         = StitchOutlineVariant,

    error                  = StitchError,
    onError                = StitchOnError,
    errorContainer         = StitchErrorContainer,
    onErrorContainer       = StitchOnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary                = StitchPrimaryDark,
    onPrimary              = StitchOnPrimaryDark,
    primaryContainer       = StitchPrimaryContainerDark,
    onPrimaryContainer     = StitchOnPrimaryContainerDark,

    secondary              = StitchSecondaryDark,
    onSecondary            = StitchOnSecondaryDark,
    secondaryContainer     = StitchSecondaryContainerDark,
    onSecondaryContainer   = StitchOnSecondaryContainerDark,

    tertiary               = StitchTertiaryDark,
    onTertiary             = StitchOnTertiaryDark,
    tertiaryContainer      = StitchTertiaryContainerDark,
    onTertiaryContainer    = StitchOnTertiaryContainerDark,

    background             = StitchBackgroundDark,
    onBackground           = StitchOnBackgroundDark,

    surface                = StitchSurfaceDark,
    onSurface              = StitchOnSurfaceDark,
    surfaceVariant         = StitchSurfaceVariantDark,
    onSurfaceVariant       = StitchOnSurfaceVariantDark,
    surfaceContainerLowest = StitchSurfaceContainerLowestDark,
    surfaceContainerLow    = StitchSurfaceContainerLowDark,
    surfaceContainer       = StitchSurfaceContainerDark,
    surfaceContainerHigh   = StitchSurfaceContainerHighDark,
    surfaceContainerHighest= StitchSurfaceContainerHighestDark,
    inverseSurface         = StitchInverseSurfaceDark,
    inverseOnSurface       = StitchInverseOnSurfaceDark,
    inversePrimary         = StitchInversePrimaryDark,

    outline                = StitchOutlineDark,
    outlineVariant         = StitchOutlineVariantDark,

    error                  = StitchErrorDark,
    onError                = StitchOnErrorDark,
    errorContainer         = StitchErrorContainerDark,
    onErrorContainer       = StitchOnErrorContainerDark,
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
