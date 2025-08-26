import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class NotificationBadge {
  static const MethodChannel _channel = MethodChannel('notification_badge');

  /// Sets the badge count on the app icon
  /// Returns true if successful, false otherwise
  static Future<bool> setBadgeCount(int count) async {
    if (count < 0) {
      throw ArgumentError('Badge count cannot be negative');
    }
    
    try {
      final result = await _channel.invokeMethod(
        'setBadgeCount',
        {'count': count},
      );
      return result == true;
    } catch (e) {
      if (kDebugMode) {
        print('NotificationBadge: Failed to set badge count: $e');
      }
      return false;
    }
  }

  /// Gets the current badge count
  /// Returns the current badge count, or 0 if unable to retrieve
  static Future<int> getBadgeCount() async {
    try {
      final result = await _channel.invokeMethod<int>('getBadgeCount');
      return result ?? 0;
    } catch (e) {
      if (kDebugMode) {
        print('NotificationBadge: Failed to get badge count: $e');
      }
      return 0;
    }
  }

  /// Clears the badge count (sets it to 0)
  /// Returns true if successful, false otherwise
  static Future<bool> clearBadge() async {
    return setBadgeCount(0);
  }

  /// Checks if the device supports badge notifications
  /// Returns true if supported, false otherwise
  static Future<bool> isSupported() async {
    try {
      final result = await _channel.invokeMethod('isSupported');
      return result == true;
    } catch (e) {
      if (kDebugMode) {
        print('NotificationBadge: Failed to check support: $e');
      }
      return false;
    }
  }

  /// Increments the badge count by 1
  /// Returns the new badge count, or current count if failed
  static Future<int> incrementBadge() async {
    try {
      final currentCount = await getBadgeCount();
      final success = await setBadgeCount(currentCount + 1);
      return success ? currentCount + 1 : currentCount;
    } catch (e) {
      if (kDebugMode) {
        print('NotificationBadge: Failed to increment badge: $e');
      }
      return await getBadgeCount();
    }
  }

  /// Decrements the badge count by 1 (minimum 0)
  /// Returns the new badge count, or current count if failed
  static Future<int> decrementBadge() async {
    try {
      final currentCount = await getBadgeCount();
      final newCount = (currentCount - 1).clamp(0, double.infinity).toInt();
      final success = await setBadgeCount(newCount);
      return success ? newCount : currentCount;
    } catch (e) {
      if (kDebugMode) {
        print('NotificationBadge: Failed to decrement badge: $e');
      }
      return await getBadgeCount();
    }
  }

  /// Gets device manufacturer information (Android only)
  /// Returns manufacturer name, or empty string if not available
  static Future<String> getDeviceManufacturer() async {
    if (!Platform.isAndroid) return '';
    
    try {
      final result = await _channel.invokeMethod<String>(
        'getDeviceManufacturer',
      );
      return result ?? '';
    } catch (e) {
      if (kDebugMode) {
        print('NotificationBadge: Failed to get device manufacturer: $e');
      }
      return '';
    }
  }
}