package com.example.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import org.junit.Test
import org.junit.runner.RunWith

import com.vobec.androidbase.MainActivity

@RunWith(AndroidJUnit4::class)
class LaunchSmokeTest {
    @Test
    fun launchesMainActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { /* ok */ }
        }
    }
}
