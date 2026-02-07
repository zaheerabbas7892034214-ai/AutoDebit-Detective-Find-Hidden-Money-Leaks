package com.zaheer.autodebitdetective.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zaheer.autodebitdetective.R
import com.zaheer.autodebitdetective.data.local.AutoDebitDatabase
import com.zaheer.autodebitdetective.data.datastore.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class AlertWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val database = AutoDebitDatabase.getDatabase(context)
    private val recurringDao = database.recurringDao()
    private val preferencesManager = PreferencesManager(context)
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting alert check...")
            
            val alertsEnabled = preferencesManager.alertsEnabled.first()
            if (!alertsEnabled) {
                Log.d(TAG, "Alerts disabled by user")
                return@withContext Result.success()
            }
            
            createNotificationChannel()
            
            checkUpcomingCharges()
            checkLargeCharges()
            checkMultipleChargesThisWeek()
            
            Log.d(TAG, "Alert check complete")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during alert check", e)
            Result.retry()
        }
    }
    
    private suspend fun checkUpcomingCharges() {
        val upcomingDaysThreshold = preferencesManager.alertDaysBefore.first()
        val currentTime = System.currentTimeMillis()
        val thresholdTime = currentTime + TimeUnit.DAYS.toMillis(upcomingDaysThreshold.toLong())
        
        val allRecurring = recurringDao.getAllRecurringItems()
        
        val upcomingCharges = allRecurring.filter { item ->
            item.isAlertEnabled &&
            item.nextPredictedEpoch in currentTime..thresholdTime
        }
        
        if (upcomingCharges.isNotEmpty()) {
            val hideContent = preferencesManager.hideNotificationContent.first()
            
            if (upcomingCharges.size == 1) {
                val item = upcomingCharges.first()
                sendUpcomingChargeNotification(item.merchant, item.avgAmount, hideContent)
            } else {
                sendMultipleUpcomingChargesNotification(upcomingCharges.size, hideContent)
            }
        }
    }
    
    private suspend fun checkLargeCharges() {
        val largeChargeThreshold = preferencesManager.largeChargeThreshold.first()
        if (largeChargeThreshold <= 0.0) return
        
        val currentTime = System.currentTimeMillis()
        val nextWeek = currentTime + TimeUnit.DAYS.toMillis(7)
        
        val allRecurring = recurringDao.getAllRecurringItems()
        
        val largeCharges = allRecurring.filter { item ->
            item.isAlertEnabled &&
            item.avgAmount >= largeChargeThreshold &&
            item.nextPredictedEpoch in currentTime..nextWeek
        }
        
        if (largeCharges.isNotEmpty()) {
            val hideContent = preferencesManager.hideNotificationContent.first()
            
            largeCharges.forEach { item ->
                sendLargeChargeNotification(item.merchant, item.avgAmount, hideContent)
            }
        }
    }
    
    private suspend fun checkMultipleChargesThisWeek() {
        val currentTime = System.currentTimeMillis()
        val endOfWeek = currentTime + TimeUnit.DAYS.toMillis(7)
        
        val allRecurring = recurringDao.getAllRecurringItems()
        
        val chargesThisWeek = allRecurring.filter { item ->
            item.isAlertEnabled &&
            item.nextPredictedEpoch in currentTime..endOfWeek
        }
        
        if (chargesThisWeek.size >= 3) {
            val totalAmount = chargesThisWeek.sumOf { it.avgAmount }
            val hideContent = preferencesManager.hideNotificationContent.first()
            
            sendWeeklyChargesNotification(chargesThisWeek.size, totalAmount, hideContent)
        }
    }
    
    private fun sendUpcomingChargeNotification(merchant: String, amount: Double, hideContent: Boolean) {
        val title = if (hideContent) {
            "Upcoming Charge"
        } else {
            "Upcoming charge from $merchant"
        }
        
        val content = if (hideContent) {
            "Tap to view details"
        } else {
            "Amount: ${formatCurrency(amount)}"
        }
        
        sendNotification(
            notificationId = merchant.hashCode(),
            title = title,
            content = content,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }
    
    private fun sendLargeChargeNotification(merchant: String, amount: Double, hideContent: Boolean) {
        val title = if (hideContent) {
            "Large Charge Alert"
        } else {
            "Large charge: ${formatCurrency(amount)}"
        }
        
        val content = if (hideContent) {
            "Tap to view details"
        } else {
            "From $merchant"
        }
        
        sendNotification(
            notificationId = (merchant + "_large").hashCode(),
            title = title,
            content = content,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    private fun sendMultipleUpcomingChargesNotification(count: Int, hideContent: Boolean) {
        val title = if (hideContent) {
            "Multiple Charges"
        } else {
            "$count charges coming up"
        }
        
        val content = if (hideContent) {
            "Tap to view details"
        } else {
            "Check your upcoming charges"
        }
        
        sendNotification(
            notificationId = NOTIFICATION_ID_MULTIPLE,
            title = title,
            content = content,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }
    
    private fun sendWeeklyChargesNotification(count: Int, totalAmount: Double, hideContent: Boolean) {
        val title = if (hideContent) {
            "Weekly Charges"
        } else {
            "$count charges this week"
        }
        
        val content = if (hideContent) {
            "Tap to view details"
        } else {
            "Total: ${formatCurrency(totalAmount)}"
        }
        
        sendNotification(
            notificationId = NOTIFICATION_ID_WEEKLY,
            title = title,
            content = content,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }
    
    private fun sendNotification(
        notificationId: Int,
        title: String,
        content: String,
        priority: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Notification permission not granted")
                return
            }
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(priority)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to send notification", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recurring Charge Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming recurring charges"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return format.format(amount)
    }
    
    companion object {
        private const val TAG = "AlertWorker"
        const val WORK_NAME = "alert_work"
        private const val CHANNEL_ID = "recurring_alerts"
        private const val NOTIFICATION_ID_MULTIPLE = 1000
        private const val NOTIFICATION_ID_WEEKLY = 1001
    }
}
