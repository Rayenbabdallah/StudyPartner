package com.example.studypartner

import androidx.compose.runtime.compositionLocalOf

/** Provides the tunisian-mode flag down the composition tree without prop-drilling. */
val LocalTunisianMode = compositionLocalOf { false }

data class StringSet(
    val modeName: String,
    val bannerTitle: String,
    val bannerSubtitle: String,
    val focus1: String,
    val focus2: String,
    val focus3: String,
    val rescueCta: String,
    val doNow: String,
    val ifTime: String,
    val skip: String
)

object
AppStrings {

    val normal = StringSet(
        modeName       = "Rescue Mode",
        bannerTitle    = "Last-Minute Mode Activated ⚠",
        bannerSubtitle = "Survival optimization is now active. Focus on what matters most.",
        focus1         = "Focus. One task at a time.",
        focus2         = "Forget perfection. Deliver something.",
        focus3         = "You've got this. Stay locked in.",
        rescueCta      = "Generate Rescue Plan",
        doNow          = "DO NOW",
        ifTime         = "IF TIME",
        skip           = "SKIP"
    )

    val tunisian = StringSet(
        modeName       = "Ghasret Lekleb",
        bannerTitle    = "Ghasret Lekleb Mode 😅",
        bannerSubtitle = "Matkhalich rouhek tghrok — we got this. Survival mode active.",
        focus1         = "ghasra w tetaada",
        focus2         = "tjibha nchalah",
        focus3         = "tgued rouhek",
        rescueCta      = "plan 🆘",
        doNow          = "taw tguedha",
        ifTime         = "ken bkalek wakt",
        skip           = "sayeb aalik"
    )

    fun get(tunisian: Boolean): StringSet = if (tunisian) this.tunisian else normal
}
