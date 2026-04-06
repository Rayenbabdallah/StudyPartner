package com.example.studypartner

enum class Level(val value: Int, val label: String) {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    CRITICAL(4, "Critical"),
    EXTREME(5, "Extreme");

    companion object {
        fun fromValue(value: Int): Level = entries.first { it.value == value }
    }
}
