package com.example.studypartner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EnumsTest {

    @Test
    fun level_fromValue_roundTripsAllEntries() {
        Level.entries.forEach { level ->
            assertSame(level, Level.fromValue(level.value))
        }
    }

    @Test
    fun taskType_fromName_roundTripsAllEntries() {
        TaskType.entries.forEach { type ->
            assertSame(type, TaskType.fromName(type.name))
        }
    }

    @Test
    fun taskType_fromName_defaultsToAssignmentOnUnknown() {
        assertEquals(TaskType.ASSIGNMENT, TaskType.fromName("not-a-type"))
        assertEquals(TaskType.ASSIGNMENT, TaskType.fromName(""))
    }

    @Test
    fun gradeImpact_fromWeight_picksNearestBucket() {
        assertEquals(GradeImpact.MINIMAL,  GradeImpact.fromWeight(0.10f))
        assertEquals(GradeImpact.LOW,      GradeImpact.fromWeight(0.25f))
        assertEquals(GradeImpact.MEDIUM,   GradeImpact.fromWeight(0.50f))
        assertEquals(GradeImpact.HIGH,     GradeImpact.fromWeight(0.75f))
        assertEquals(GradeImpact.CRITICAL, GradeImpact.fromWeight(1.0f))
    }

    @Test
    fun gradeImpact_fromWeight_snapsToClosest_forOffValues() {
        assertEquals(GradeImpact.MEDIUM, GradeImpact.fromWeight(0.45f))
        assertEquals(GradeImpact.HIGH,   GradeImpact.fromWeight(0.70f))
        assertEquals(GradeImpact.MINIMAL, GradeImpact.fromWeight(0.0f))
        assertEquals(GradeImpact.CRITICAL, GradeImpact.fromWeight(2.0f))
    }
}

class AppStringsTest {

    @Test
    fun get_returnsNormalByDefault() {
        assertSame(AppStrings.normal, AppStrings.get(false))
    }

    @Test
    fun get_returnsTunisianWhenFlagged() {
        assertSame(AppStrings.tunisian, AppStrings.get(true))
    }

    @Test
    fun normal_andTunisian_haveDistinctLabels() {
        assertEquals("Rescue Mode", AppStrings.normal.modeName)
        assertEquals("Ghasret Lekleb", AppStrings.tunisian.modeName)
    }
}
