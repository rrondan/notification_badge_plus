package com.dp.notification_badge

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log

class BadgeHelper(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notification_badge", Context.MODE_PRIVATE)
    private val badgeProviders: List<BadgeProvider> = listOf(
        SamsungBadgeProvider(context),
        XiaomiBadgeProvider(context),
        HuaweiBadgeProvider(context),
        OppoBadgeProvider(context),
        VivoBadgeProvider(context),
        OnePlusBadgeProvider(context),
        SonyBadgeProvider(context),
        HTCBadgeProvider(context),
        LGBadgeProvider(context),
        NovaLauncherBadgeProvider(context),
        AndroidOreoDefaultBadgeProvider(context)
    )

    fun setBadgeCount(count: Int): Boolean {
        try {
            // Store the count in SharedPreferences for persistence
            prefs.edit().putInt("badge_count", count).apply()
            
            var anySuccess = false
            for (provider in badgeProviders) {
                if (provider.isSupported()) {
                    try {
                        if (provider.setBadgeCount(count)) {
                            anySuccess = true
                            Log.d("NotificationBadge", "Successfully set badge using ${provider.javaClass.simpleName}")
                        }
                    } catch (e: Exception) {
                        Log.w("NotificationBadge", "Failed to set badge using ${provider.javaClass.simpleName}: ${e.message}")
                    }
                }
            }
            return anySuccess
        } catch (e: Exception) {
            Log.e("NotificationBadge", "Error setting badge count", e)
            return false
        }
    }

    fun getBadgeCount(): Int {
        return prefs.getInt("badge_count", 0)
    }

    fun isSupported(): Boolean {
        return badgeProviders.any { it.isSupported() }
    }

    fun getSupportedProviders(): List<String> {
        return badgeProviders.filter { it.isSupported() }.map { it.javaClass.simpleName }
    }
}