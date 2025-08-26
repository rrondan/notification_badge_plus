package com.dp.notification_badge_plus

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build

class SamsungBadgeProvider(private val context: Context) : BadgeProvider {
    
    companion object {
        private const val SAMSUNG_CONTENT_URI = "content://com.sec.android.provider.badge/apps"
        private const val SAMSUNG_BADGE_PROVIDER = "com.sec.android.provider.badge"
    }

    override fun isSupported(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val isSamsung = manufacturer.contains("samsung")
        
        if (!isSamsung) return false
        
        // Check if Samsung badge provider is available
        return try {
            val uri = Uri.parse(SAMSUNG_CONTENT_URI)
            context.contentResolver.query(uri, null, null, null, null)?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun setBadgeCount(count: Int): Boolean {
        return try {
            val uri = Uri.parse(SAMSUNG_CONTENT_URI)
            val contentValues = ContentValues().apply {
                put("package", context.packageName)
                put("class", getLauncherActivityClass())
                put("badgecount", count)
            }
            
            val result = context.contentResolver.insert(uri, contentValues)
            result != null
        } catch (e: Exception) {
            // Try alternative method for newer Samsung devices
            tryAlternativeSamsungMethod(count)
        }
    }

    private fun tryAlternativeSamsungMethod(count: Int): Boolean {
        return try {
            val uri = Uri.parse("content://com.sec.android.provider.badge/apps?notify=true")
            val contentValues = ContentValues().apply {
                put("package", context.packageName)
                put("class", getLauncherActivityClass())
                put("badgecount", count)
            }
            
            val result = context.contentResolver.insert(uri, contentValues)
            result != null
        } catch (e: Exception) {
            false
        }
    }

    private fun getLauncherActivityClass(): String {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return intent?.component?.className ?: ""
    }
}