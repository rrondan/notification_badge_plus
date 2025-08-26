package com.dp.notification_badge_plus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class AndroidOreoDefaultBadgeProvider(private val context: Context) : BadgeProvider {
    
    companion object {
        private const val CHANNEL_ID = "notification_badge_channel"
        private const val NOTIFICATION_ID = 1000
    }

    override fun isSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    override fun setBadgeCount(count: Int): Boolean {
        return try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Badge Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for showing app badge count"
                setShowBadge(true)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
            
            if (count > 0) {
                // Create a notification to trigger the badge
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Badge Count")
                    .setContentText("You have $count notifications")
                    .setNumber(count)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOngoing(false)
                    .setSilent(true)
                    .build()
                
                notificationManager.notify(NOTIFICATION_ID, notification)
            } else {
                // Clear the notification when count is 0
                notificationManager.cancel(NOTIFICATION_ID)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
}