package com.dp.notification_badge

interface BadgeProvider {
    fun isSupported(): Boolean
    fun setBadgeCount(count: Int): Boolean
}