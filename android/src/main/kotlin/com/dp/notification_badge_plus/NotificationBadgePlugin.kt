package com.dp.notification_badge_plus

import android.content.Context
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class NotificationBadgePlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel : MethodChannel
    private lateinit var context: Context
    private lateinit var badgeHelper: BadgeHelper

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "notification_badge")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        badgeHelper = BadgeHelper(context)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "setBadgeCount" -> {
                val count = call.argument<Int>("count") ?: 0
                val success = badgeHelper.setBadgeCount(count)
                result.success(success)
            }
            "getBadgeCount" -> {
                val count = badgeHelper.getBadgeCount()
                result.success(count)
            }
            "isSupported" -> {
                val supported = badgeHelper.isSupported()
                result.success(supported)
            }
            "getDeviceManufacturer" -> {
                val manufacturer = Build.MANUFACTURER
                result.success(manufacturer)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}