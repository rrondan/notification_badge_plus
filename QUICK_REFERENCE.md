# Notification Badge - Quick Reference Guide

## 🚀 Quick Setup

```dart
import 'package:notification_badge/notification_badge.dart';

// Set badge count
await NotificationBadge.setBadgeCount(5);

// Get current count
int count = await NotificationBadge.getBadgeCount();

// Clear badge
await NotificationBadge.clearBadge();
```

## 📱 App Lifecycle Integration

### Basic Setup
```dart
class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        _syncBadgeOnResume();
        break;
      case AppLifecycleState.paused:
        _saveBadgeState();
        break;
      default:
        break;
    }
  }
}
```

## 🔄 Background/Foreground Scenarios

### Scenario 1: App Receives Push Notification While in Background

**What happens:**
- iOS: Badge updates automatically, visible immediately
- Android: Depends on OEM implementation

**Code:**
```dart
// Background message handler (Firebase)
static Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  final badgeCount = int.tryParse(message.data['badge'] ?? '1') ?? 1;
  final currentCount = await NotificationBadge.getBadgeCount();
  await NotificationBadge.setBadgeCount(currentCount + badgeCount);
}
```

### Scenario 2: User Opens App After Receiving Notifications

**What happens:**
- Badge count should sync with actual unread items
- Any pending updates should be applied

**Code:**
```dart
Future<void> _syncBadgeOnResume() async {
  // Get actual unread count from your data source
  final unreadCount = await _getUnreadItemsCount();
  
  // Update badge to match actual count
  await NotificationBadge.setBadgeCount(unreadCount);
}
```

### Scenario 3: User Reads Items While App is Open

**What happens:**
- Badge should decrease immediately
- Changes should persist when app goes to background

**Code:**
```dart
Future<void> markItemAsRead() async {
  // Mark item as read in your data
  await _markItemRead();
  
  // Decrement badge
  await NotificationBadge.decrementBadge();
}
```

### Scenario 4: App is Terminated and Restarted

**What happens:**
- Badge count should be restored from persistent storage
- Sync with actual data state

**Code:**
```dart
// In main() or initState()
Future<void> _initializeBadge() async {
  final savedCount = await NotificationBadge.getBadgeCount();
  final actualCount = await _getUnreadItemsCount();
  
  if (savedCount != actualCount) {
    // Sync if there's a mismatch
    await NotificationBadge.setBadgeCount(actualCount);
  }
}
```

## 🔧 Platform-Specific Handling

### iOS
```dart
// iOS handles badges natively - works in all states
await NotificationBadge.setBadgeCount(count); // ✅ Always works
```

### Android - Samsung
```dart
// Works reliably in background
await NotificationBadge.setBadgeCount(count); // ✅ Usually works
```

### Android - Xiaomi/MIUI
```dart
// Requires notification to show badge
if (manufacturer.contains('xiaomi')) {
  // Badge shows only when notification is present
  await NotificationBadge.setBadgeCount(count);
  // Badge will be visible through notification system
}
```

### Android - Other OEMs
```dart
// Check support first
final isSupported = await NotificationBadge.isSupported();
if (isSupported) {
  await NotificationBadge.setBadgeCount(count);
} else {
  // Show alternative UI indicator
  _showInAppBadgeIndicator(count);
}
```

## ⚡ Common Patterns

### Pattern 1: Push Notification + Badge Update
```dart
class NotificationHandler {
  static Future<void> handleNotification(RemoteMessage message) async {
    // Extract badge count from payload
    final badgeCount = int.tryParse(message.data['badge'] ?? '1') ?? 1;
    
    // Update badge
    await NotificationBadge.incrementBadge();
    
    // Show local notification if app is in foreground
    if (WidgetsBinding.instance.lifecycleState == AppLifecycleState.resumed) {
      _showLocalNotification(message);
    }
  }
}
```

### Pattern 2: Badge Reset on App Open
```dart
class AppStateManager {
  static Future<void> onAppOpened() async {
    // Clear all badges when user opens app
    await NotificationBadge.clearBadge();
    
    // Mark all notifications as seen in your backend
    await _markAllNotificationsAsSeen();
  }
}
```

### Pattern 3: Gradual Badge Decrease
```dart
class MessageManager {
  static Future<void> markMessageAsRead(String messageId) async {
    // Mark message as read
    await _markAsRead(messageId);
    
    // Decrease badge by 1
    final newCount = await NotificationBadge.decrementBadge();
    print('Badge count now: $newCount');
  }
}
```

### Pattern 4: Badge Synchronization
```dart
class BadgeSync {
  static Future<void> syncWithServer() async {
    try {
      // Get unread count from server
      final serverUnreadCount = await _getUnreadCountFromServer();
      
      // Get current badge count
      final currentBadgeCount = await NotificationBadge.getBadgeCount();
      
      if (serverUnreadCount != currentBadgeCount) {
        // Update badge to match server
        await NotificationBadge.setBadgeCount(serverUnreadCount);
        print('Badge synced: $serverUnreadCount');
      }
    } catch (e) {
      print('Sync failed: $e');
    }
  }
}
```

## 🐛 Troubleshooting Quick Fixes

### Issue: Badge not showing on Android
```dart
// Check device support
final supported = await NotificationBadge.isSupported();
final manufacturer = await NotificationBadge.getDeviceManufacturer();
print('Supported: $supported, Manufacturer: $manufacturer');
```

### Issue: Badge count mismatch after app resume
```dart
@override
void didChangeAppLifecycleState(AppLifecycleState state) {
  if (state == AppLifecycleState.resumed) {
    // Always sync on resume
    Timer(Duration(milliseconds: 500), () async {
      final actualCount = await _getActualUnreadCount();
      await NotificationBadge.setBadgeCount(actualCount);
    });
  }
}
```

### Issue: Badge persists after clearing notifications
```dart
// Force clear badge
await NotificationBadge.clearBadge();

// Wait a moment and check
await Future.delayed(Duration(milliseconds: 100));
final count = await NotificationBadge.getBadgeCount();
if (count > 0) {
  // Force clear again
  await NotificationBadge.setBadgeCount(0);
}
```

### Issue: iOS badge not showing
```dart
// Check notification permissions
import 'dart:io';

if (Platform.isIOS) {
  // Request notification permissions for iOS
  // This is required for badges to work
}
```

## 📋 Best Practices Checklist

- ✅ Always implement WidgetsBindingObserver for lifecycle management
- ✅ Sync badge count when app resumes from background
- ✅ Handle push notifications properly for both foreground and background
- ✅ Test on multiple devices and manufacturers (especially Android OEMs)
- ✅ Provide fallback UI indicators for unsupported devices
- ✅ Keep badge count in sync with actual data state
- ✅ Clear badges appropriately when user sees content
- ✅ Handle errors gracefully with try-catch blocks
- ✅ Use debouncing for rapid badge updates
- ✅ Test badge persistence across app restarts

## 📖 Additional Resources

- [Complete Background/Foreground Guide](BACKGROUND_FOREGROUND_GUIDE.md) - Detailed implementation guide
- [Main README](README.md) - Full documentation and API reference
- [Example App](example/lib/main.dart) - Working example with all features