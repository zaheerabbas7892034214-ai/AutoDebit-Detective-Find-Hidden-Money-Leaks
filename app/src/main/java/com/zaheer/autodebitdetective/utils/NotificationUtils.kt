package com.zaheer.autodebitdetective.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zaheer.autodebitdetective.MainActivity
import com.zaheer.autodebitdetective.R

object NotificationUtils {
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val alertsChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ALERTS,
                Constants.NOTIFICATION_CHANNEL_ALERTS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constants.NOTIFICATION_CHANNEL_ALERTS_DESC
                enableVibration(true)
                enableLights(true)
            }
            
            val scanChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SCAN,
                Constants.NOTIFICATION_CHANNEL_SCAN_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = Constants.NOTIFICATION_CHANNEL_SCAN_DESC
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(scanChannel)
        }
    }
    
    fun showPaymentAlertNotification(
        context: Context,
        title: String,
        message: String,
        hideContent: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        
        if (hideContent) {
            notificationBuilder
                .setContentTitle("Payment Reminder")
                .setContentText("You have an upcoming payment")
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(
                    NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ALERTS)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Payment Reminder")
                        .setContentText("Tap to view details")
                        .build()
                )
        } else {
            notificationBuilder
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Constants.NOTIFICATION_ID_ALERT, notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted, handle silently
        }
    }
    
    fun showScanProgressNotification(
        context: Context,
        progress: Int,
        total: Int
    ) {
        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_SCAN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scanning SMS")
            .setContentText("Processing messages...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(total, progress, false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Constants.NOTIFICATION_ID_SCAN, notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted, handle silently
        }
    }
    
    fun showScanCompleteNotification(
        context: Context,
        transactionCount: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val message = if (transactionCount == 0) {
            "No new transactions found"
        } else {
            "Found $transactionCount transaction${if (transactionCount > 1) "s" else ""}"
        }
        
        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_SCAN)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scan Complete")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Constants.NOTIFICATION_ID_SCAN, notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted, handle silently
        }
    }
    
    fun cancelScanNotification(context: Context) {
        try {
            with(NotificationManagerCompat.from(context)) {
                cancel(Constants.NOTIFICATION_ID_SCAN)
            }
        } catch (e: SecurityException) {
            // Permission not granted, handle silently
        }
    }
    
    fun cancelAllNotifications(context: Context) {
        try {
            with(NotificationManagerCompat.from(context)) {
                cancelAll()
            }
        } catch (e: SecurityException) {
            // Permission not granted, handle silently
        }
    }
    
    fun areNotificationsEnabled(context: Context): Boolean {
        return try {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } catch (e: Exception) {
            false
        }
    }
    
    fun showRecurringDetectedNotification(
        context: Context,
        merchantName: String,
        amount: Double,
        hideContent: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val formattedAmount = CurrencyUtils.formatAmount(amount)
        
        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
        
        if (hideContent) {
            notificationBuilder
                .setContentTitle("New Recurring Payment Detected")
                .setContentText("Tap to view details")
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        } else {
            notificationBuilder
                .setContentTitle("New Recurring Payment Detected")
                .setContentText("$merchantName - $formattedAmount")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("We detected a recurring payment pattern for $merchantName with amount $formattedAmount")
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Constants.NOTIFICATION_ID_ALERT + 1, notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted, handle silently
        }
    }
    
    fun showMultiplePaymentAlertsNotification(
        context: Context,
        count: Int,
        totalAmount: Double,
        hideContent: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val formattedAmount = CurrencyUtils.formatAmount(totalAmount)
        
        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        
        if (hideContent) {
            notificationBuilder
                .setContentTitle("Upcoming Payments")
                .setContentText("You have $count upcoming payments")
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        } else {
            notificationBuilder
                .setContentTitle("$count Upcoming Payments")
                .setContentText("Total: $formattedAmount")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("You have $count payments due soon with total amount of $formattedAmount")
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(Constants.NOTIFICATION_ID_ALERT + 2, notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted, handle silently
        }
    }
}
