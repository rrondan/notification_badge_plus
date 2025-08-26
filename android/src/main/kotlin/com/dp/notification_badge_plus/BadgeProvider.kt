package com.dp.notification_badge_plus

interface BadgeProvider {
    fun isSupported(): Boolean
    fun setBadgeCount(count: Int): Boolean
}