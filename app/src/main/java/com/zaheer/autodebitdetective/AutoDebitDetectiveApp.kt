package com.zaheer.autodebitdetective

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zaheer.autodebitdetective.data.local.AutoDebitDatabase
import com.zaheer.autodebitdetective.utils.Constants
import com.zaheer.autodebitdetective.utils.NotificationUtils
import com.zaheer.autodebitdetective.worker.AlertWorker
import com.zaheer.autodebitdetective.worker.RecurringScanWorker
import java.util.concurrent.TimeUnit

class AutoDebitDetectiveApp : Application(), Configuration.Provider {
    
    lateinit var database: AutoDebitDatabase
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        initializeDatabase()
        createNotificationChannels()
        schedulePeriodicWork()
    }
    
    private fun initializeDatabase() {
        database = AutoDebitDatabase.getDatabase(this)
    }
    
    private fun createNotificationChannels() {
        NotificationUtils.createNotificationChannels(this)
    }
    
    private fun schedulePeriodicWork() {
        val workManager = WorkManager.getInstance(this)
        
        val scanConstraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val scanWorkRequest = PeriodicWorkRequestBuilder<RecurringScanWorker>(
            Constants.DEFAULT_SCAN_FREQUENCY_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(scanConstraints)
            .addTag(Constants.WORK_TAG_SCAN)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_TAG_SCAN,
            ExistingPeriodicWorkPolicy.KEEP,
            scanWorkRequest
        )
        
        val alertConstraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        val alertWorkRequest = PeriodicWorkRequestBuilder<AlertWorker>(
            24,
            TimeUnit.HOURS
        )
            .setConstraints(alertConstraints)
            .addTag(Constants.WORK_TAG_ALERT)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_TAG_ALERT,
            ExistingPeriodicWorkPolicy.KEEP,
            alertWorkRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
