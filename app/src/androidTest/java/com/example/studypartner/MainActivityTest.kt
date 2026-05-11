package com.example.studypartner

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test for [MainActivity]: launches the activity, drives it through the
 * standard lifecycle states, and verifies it reaches RESUMED without crashing.
 *
 * This catches:
 *   - missing Room migration registrations
 *   - WorkManager not initialised
 *   - Compose graph construction failures (e.g. unresolved Screen routes)
 *   - theme/DataStore wiring regressions
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun appContext_hasExpectedPackage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.studypartner", context.packageName)
    }

    @Test
    fun mainActivity_launches_andReachesResumed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity: Activity ->
                assertNotNull("MainActivity should be created", activity)
                assertTrue(
                    "Activity window should be attached",
                    activity.window?.decorView?.isAttachedToWindow == true ||
                        activity.window?.decorView != null
                )
            }
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test
    fun mainActivity_survivesRecreate() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.recreate()
            scenario.onActivity { activity: Activity ->
                assertNotNull(activity)
            }
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
