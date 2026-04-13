package com.example.studypartner

import kotlin.math.abs

enum class GradeImpact(val weight: Float, val label: String) {
    MINIMAL(0.10f, "Minimal"),
    LOW(0.25f,     "Low"),
    MEDIUM(0.50f,  "Medium"),
    HIGH(0.75f,    "High"),
    CRITICAL(1.0f, "Critical");

    companion object {
        fun fromWeight(weight: Float): GradeImpact =
            entries.minByOrNull { abs(it.weight - weight) } ?: MEDIUM
    }
}
