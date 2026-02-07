package com.zaheer.autodebitdetective

import android.app.Application
import androidx.work.Configuration

class AutoDebitDetectiveApp : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize app-level dependencies here
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
