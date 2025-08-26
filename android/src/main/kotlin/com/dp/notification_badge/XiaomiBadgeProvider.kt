package com.dp.notification_badge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class XiaomiBadgeProvider(private val context: Context) : BadgeProvider {
    
    companion object {
        private const val CHANNEL_ID = "badge_notification_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun isSupported(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer.contains("xiaomi") || 
               manufacturer.contains("redmi") || 
               manufacturer.contains("poco") ||
               isXiaomiDevice()
    }

    override fun setBadgeCount(count: Int): Boolean {
        return try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Badge Notifications",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notifications for app badge count"
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            if (count > 0) {
                // Create notification with badge
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .setContentTitle("Badge Count")
                    .setContentText("You have $count notifications")
                    .setNumber(count)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build()
                
                notificationManager.notify(NOTIFICATION_ID, notification)
            } else {
                // Clear notification when count is 0
                notificationManager.cancel(NOTIFICATION_ID)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isXiaomiDevice(): Boolean {
        return try {
            val properties = System.getProperties()
            val miuiVersion = properties.getProperty("ro.miui.ui.version.name")
            miuiVersion != null
        } catch (e: Exception) {
            false
        }
    }
}