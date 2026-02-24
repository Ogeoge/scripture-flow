package com.scriptureflow

import android.app.Application
import com.scriptureflow.data.db.AppDatabase
import com.scriptureflow.data.prefs.ReadingPrefsStore
import com.scriptureflow.data.repo.ScriptureRepository

class App : Application() {

    // Lazy singletons for a minimal first version.
    // Note: This app is offline-first; no network initialization is required.

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val readingPrefsStore: ReadingPrefsStore by lazy {
        ReadingPrefsStore(this)
    }

    val scriptureRepository: ScriptureRepository by lazy {
        ScriptureRepository(
            context = this,
            database = database,
            readingPrefsStore = readingPrefsStore,
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Intentionally minimal. Repository seeding is triggered on-demand.
    }
}
