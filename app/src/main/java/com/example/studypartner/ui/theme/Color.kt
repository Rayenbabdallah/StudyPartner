package com.example.studypartner.ui.theme

import androidx.compose.ui.graphics.Color

// ── StudyPartner / "Sun-Soaked Paper" palette ─────────────────────────────────
// Warm orange + gold + lime, on soft paper-white. Editorial, not generic.
//
// User-supplied reference:
//   Deep Orange   #FF9F1C   primary brand
//   Warm Amber    #FFB627   secondary brand
//   Soft Gold     #FFE066   primary container (hero highlights)
//   Bookmark Gold #FFC300   accent (bookmarks, ribbons)
//   Lime Check    #A8D62F   tertiary / "done" / safe-status
//   Orange Check  #FFAA1D   warning / moderate-risk accent
//   Soft White    #F8F7F5   background
//   Stroke        #D9D4CF   outline-variant
//   Shadow        #B8B1AA   outline

// ── Brand accents (use directly when M3 slot doesn't fit) ────────────────────
val DeepOrange   = Color(0xFFFF9F1C)
val WarmAmber    = Color(0xFFFFB627)
val SoftGold     = Color(0xFFFFE066)
val BookmarkGold = Color(0xFFFFC300)
val LimeCheck    = Color(0xFFA8D62F)
val OrangeCheck  = Color(0xFFFFAA1D)
val SoftWhite    = Color(0xFFF8F7F5)
val StrokeGray   = Color(0xFFD9D4CF)
val ShadowGray   = Color(0xFFB8B1AA)

// ── M3 Light scheme ──────────────────────────────────────────────────────────
val StitchPrimary              = DeepOrange
val StitchOnPrimary            = Color(0xFFFFFFFF)
val StitchPrimaryContainer     = SoftGold
val StitchOnPrimaryContainer   = Color(0xFF3D2400)

val StitchSecondary            = WarmAmber
val StitchOnSecondary          = Color(0xFFFFFFFF)
val StitchSecondaryContainer   = Color(0xFFFFF1CF)
val StitchOnSecondaryContainer = Color(0xFF4A2E00)

val StitchTertiary             = LimeCheck
val StitchOnTertiary           = Color(0xFF1A2C00)
val StitchTertiaryContainer    = Color(0xFFE5F5C0)
val StitchOnTertiaryContainer  = Color(0xFF1A2C00)

val StitchBackground           = SoftWhite
val StitchOnBackground         = Color(0xFF2A2622)

val StitchSurface                  = SoftWhite
val StitchOnSurface                = Color(0xFF2A2622)
val StitchSurfaceVariant           = Color(0xFFEFEAE5)
val StitchOnSurfaceVariant         = Color(0xFF6B655F)
val StitchSurfaceContainerLowest   = Color(0xFFFFFFFF)
val StitchSurfaceContainerLow      = Color(0xFFFBFAF8)
val StitchSurfaceContainer         = Color(0xFFF4F1ED)
val StitchSurfaceContainerHigh     = Color(0xFFEFEAE5)
val StitchSurfaceContainerHighest  = Color(0xFFE8E2DC)
val StitchInverseSurface           = Color(0xFF302C28)
val StitchInverseOnSurface         = Color(0xFFF4F0EB)
val StitchInversePrimary           = SoftGold

val StitchOutline        = ShadowGray
val StitchOutlineVariant = StrokeGray

val StitchError              = Color(0xFFC9302C)
val StitchOnError            = Color(0xFFFFFFFF)
val StitchErrorContainer     = Color(0xFFFFD9D6)
val StitchOnErrorContainer   = Color(0xFF410005)

// ── M3 Dark scheme ───────────────────────────────────────────────────────────
val StitchPrimaryDark              = WarmAmber
val StitchOnPrimaryDark            = Color(0xFF3D2400)
val StitchPrimaryContainerDark     = Color(0xFF5A3A00)
val StitchOnPrimaryContainerDark   = SoftGold

val StitchSecondaryDark            = Color(0xFFFFC857)
val StitchOnSecondaryDark          = Color(0xFF3D2400)
val StitchSecondaryContainerDark   = Color(0xFF4A2E00)
val StitchOnSecondaryContainerDark = Color(0xFFFFE3A0)

val StitchTertiaryDark             = Color(0xFFC5E866)
val StitchOnTertiaryDark           = Color(0xFF1A2C00)
val StitchTertiaryContainerDark    = Color(0xFF2A3E00)
val StitchOnTertiaryContainerDark  = Color(0xFFDBF09F)

val StitchBackgroundDark           = Color(0xFF1A1714)
val StitchOnBackgroundDark         = Color(0xFFECE6DE)

val StitchSurfaceDark                  = Color(0xFF1A1714)
val StitchOnSurfaceDark                = Color(0xFFECE6DE)
val StitchSurfaceVariantDark           = Color(0xFF3A342E)
val StitchOnSurfaceVariantDark         = Color(0xFFC9C2B8)
val StitchSurfaceContainerLowestDark   = Color(0xFF100E0C)
val StitchSurfaceContainerLowDark      = Color(0xFF221F1B)
val StitchSurfaceContainerDark         = Color(0xFF2A2622)
val StitchSurfaceContainerHighDark     = Color(0xFF36312C)
val StitchSurfaceContainerHighestDark  = Color(0xFF423D37)
val StitchInverseSurfaceDark           = Color(0xFFECE6DE)
val StitchInverseOnSurfaceDark         = Color(0xFF302C28)
val StitchInversePrimaryDark           = DeepOrange

val StitchOutlineDark        = Color(0xFF968F87)
val StitchOutlineVariantDark = Color(0xFF5A544D)

val StitchErrorDark              = Color(0xFFFFB4AB)
val StitchOnErrorDark            = Color(0xFF690008)
val StitchErrorContainerDark     = Color(0xFF93000F)
val StitchOnErrorContainerDark   = Color(0xFFFFDAD6)

// ── Semantic: risk / status (light) ──────────────────────────────────────────
val RiskHighColor     = Color(0xFFC9302C)
val RiskHighBg        = Color(0xFFFFEDEB)
val RiskHighContainer = Color(0xFFFFD0CB)

val RiskMedColor      = OrangeCheck
val RiskMedBg         = Color(0xFFFFF4DC)
val RiskMedContainer  = Color(0xFFFFE3A8)

val RiskSafeColor     = Color(0xFF6B9E1A)
val RiskSafeBg        = Color(0xFFF1F8DD)
val RiskSafeContainer = Color(0xFFD9EE9B)

// ── Semantic: risk / status (dark) ───────────────────────────────────────────
val RiskHighColorDark     = Color(0xFFFFB4AB)
val RiskHighBgDark        = Color(0xFF3E1010)
val RiskHighContainerDark = Color(0xFF5C1A1A)

val RiskMedColorDark      = Color(0xFFFFD08A)
val RiskMedBgDark         = Color(0xFF2D1A00)
val RiskMedContainerDark  = Color(0xFF4A2D00)

val RiskSafeColorDark     = Color(0xFFC5E866)
val RiskSafeBgDark        = Color(0xFF192410)
val RiskSafeContainerDark = Color(0xFF2C3E1B)
