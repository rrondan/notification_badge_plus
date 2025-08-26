# Server-Side Payload Guide for Background Badge Updates

This guide provides comprehensive information for server developers on how to configure push notification payloads to enable automatic badge updates when the app is in the background.

## Table of Contents
- [Overview](#overview)
- [iOS Badge Payloads](#ios-badge-payloads)
- [Android Badge Payloads](#android-badge-payloads)
- [Firebase Cloud Messaging (FCM) Examples](#firebase-cloud-messaging-fcm-examples)
- [Apple Push Notification Service (APNs) Examples](#apple-push-notification-service-apns-examples)
- [Cross-Platform Payloads](#cross-platform-payloads)
- [Advanced Badge Scenarios](#advanced-badge-scenarios)
- [Testing and Validation](#testing-and-validation)

## Overview

The `notification_badge_plus` plugin automatically handles background badge updates when properly configured server-side payloads are received. The key is including badge information in the push notification payload that the plugin can process.

### Key Requirements:
- **iOS**: Requires `badge` field in APNs payload
- **Android**: Requires badge data in FCM data payload
- **Background Processing**: App handles badge updates even when not active
- **Automatic Sync**: Plugin syncs badge count when app returns to foreground

## iOS Badge Payloads

### Basic iOS Badge Payload

#### APNs HTTP/2 Payload Structure:
```json
{
  "aps": {
    "alert": {
      "title": "New Message",
      "body": "You have a new message"
    },
    "badge": 5,
    "sound": "default"
  }
}
```

#### Key iOS Payload Fields:
- **`badge`**: Integer value for the badge count
- **`alert`**: Optional notification content
- **`sound`**: Optional sound file name

### iOS Badge-Only Updates (Silent Notifications)

For badge updates without showing notification:

```json
{
  "aps": {
    "badge": 3,
    "content-available": 1
  }
}
```

#### Silent Notification Requirements:
- **`content-available: 1`**: Enables background processing
- **No `alert` or `sound`**: Makes notification silent
- **`badge`**: Updates badge count without user disturbance

### iOS Incremental Badge Updates

For incrementing existing badge count:

```json
{
  "aps": {
    "alert": {
      "title": "New Email",
      "body": "You have new emails"
    },
    "badge": "+1",
    "sound": "default"
  },
  "increment": true
}
```

**Note**: iOS badge field expects absolute numbers. The `"+1"` and `increment` are processed by the app, not iOS system.

## Android Badge Payloads

### Basic Android Badge Payload (FCM)

#### FCM Data Message Structure:
```json
{
  "message": {
    "token": "device_fcm_token_here",
    "data": {
      "title": "New Message",
      "body": "You have a new message", 
      "badge": "5",
      "badge_action": "set"
    }
  }
}
```

#### Key Android Payload Fields:
- **`badge`**: String value for badge count (FCM data is always strings)
- **`badge_action`**: Action type ("set", "increment", "decrement", "clear")
- **`title`** & **`body`**: Notification content (optional for badge-only)

### Android Badge-Only Updates (Data-Only)

For badge updates without showing notification:

```json
{
  "message": {
    "token": "device_fcm_token_here", 
    "data": {
      "badge": "3",
      "badge_action": "set",
      "silent": "true"
    }
  }
}
```

#### Data-Only Message Benefits:
- **Background processing**: Works when app is closed
- **No notification shown**: Silent badge update
- **Battery efficient**: Minimal system resource usage

### Android Incremental Badge Updates

For incrementing/decrementing badge count:

```json
{
  "message": {
    "token": "device_fcm_token_here",
    "data": {
      "title": "New Email", 
      "body": "You have 1 new email",
      "badge": "1",
      "badge_action": "increment"
    }
  }
}
```

#### Badge Action Types:
- **`"set"`**: Set badge to specific number
- **`"increment"`**: Add to current badge count
- **`"decrement"`**: Subtract from current badge count  
- **`"clear"`**: Set badge to 0

## Firebase Cloud Messaging (FCM) Examples

### FCM REST API v1 Examples

#### 1. Cross-Platform Badge Update
```json
{
  "message": {
    "token": "device_token_here",
    "notification": {
      "title": "New Message",
      "body": "You have new messages"
    },
    "data": {
      "badge": "7",
      "badge_action": "set"
    },
    "apns": {
      "payload": {
        "aps": {
          "badge": 7
        }
      }
    },
    "android": {
      "data": {
        "badge": "7",
        "badge_action": "set"
      }
    }
  }
}
```

#### 2. iOS-Specific Badge Update
```json
{
  "message": {
    "token": "ios_device_token",
    "apns": {
      "headers": {
        "apns-priority": "10"
      },
      "payload": {
        "aps": {
          "alert": {
            "title": "New Notification",
            "body": "Check your updates"
          },
          "badge": 12,
          "sound": "default"
        }
      }
    }
  }
}
```

#### 3. Android-Specific Badge Update
```json
{
  "message": {
    "token": "android_device_token",
    "android": {
      "priority": "high",
      "data": {
        "title": "New Update",
        "body": "You have updates",
        "badge": "4", 
        "badge_action": "increment"
      }
    }
  }
}
```

### FCM Topic-Based Badge Updates

For sending badge updates to multiple devices:

```json
{
  "message": {
    "topic": "user_notifications", 
    "notification": {
      "title": "System Update",
      "body": "New system notification"
    },
    "data": {
      "badge": "1",
      "badge_action": "increment"
    },
    "apns": {
      "payload": {
        "aps": {
          "badge": 1
        }
      }
    }
  }
}
```

## Apple Push Notification Service (APNs) Examples

### APNs HTTP/2 Direct Examples

#### 1. Standard Badge Notification
```json
{
  "aps": {
    "alert": {
      "title": "Meeting Reminder", 
      "body": "Your meeting starts in 15 minutes",
      "action-loc-key": "VIEW"
    },
    "badge": 2,
    "sound": "meeting_alert.wav",
    "category": "MEETING_REMINDER"
  },
  "meeting_id": "12345",
  "custom_data": "additional_info"
}
```

#### 2. Silent Badge Update
```json
{
  "aps": {
    "badge": 8,
    "content-available": 1
  },
  "update_type": "badge_sync",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

#### 3. Badge with Rich Notification
```json
{
  "aps": {
    "alert": {
      "title": "Photo Shared",
      "subtitle": "John Doe",
      "body": "Shared a photo with you"
    },
    "badge": 15,
    "sound": "photo_notification.wav",
    "mutable-content": 1
  },
  "attachment-url": "https://example.com/photo.jpg",
  "user_id": "user_123"
}
```

## Cross-Platform Payloads

### Unified Payload Structure

For services supporting both iOS and Android:

```json
{
  "notification": {
    "title": "New Message",
    "body": "You have received a new message"
  },
  "data": {
    "badge": "6",
    "badge_action": "set",
    "message_id": "msg_12345",
    "user_id": "user_789"
  },
  "platforms": {
    "ios": {
      "badge": 6,
      "sound": "default",
      "content-available": 1
    },
    "android": {
      "priority": "high",
      "badge_data": {
        "count": "6",
        "action": "set"
      }
    }
  }
}
```

## Advanced Badge Scenarios

### 1. User-Specific Badge Counts

For personalized badge counts based on user's unread items:

```json
{
  "message": {
    "token": "user_device_token",
    "data": {
      "badge": "{{user_unread_count}}",
      "badge_action": "set",
      "user_id": "{{user_id}}"
    },
    "apns": {
      "payload": {
        "aps": {
          "badge": "{{user_unread_count}}"
        }
      }
    }
  }
}
```

**Server-side template variables:**
- `{{user_unread_count}}`: Calculate from user's unread messages/notifications
- `{{user_id}}`: User identifier for personalization

### 2. Category-Based Badge Updates

For different types of notifications:

```json
{
  "message": {
    "token": "device_token",
    "data": {
      "badge": "3",
      "badge_action": "increment",
      "category": "messages",
      "priority": "high"
    },
    "apns": {
      "payload": {
        "aps": {
          "badge": 3,
          "category": "MESSAGE_CATEGORY"
        }
      }
    }
  }
}
```

### 3. Time-Sensitive Badge Updates

For urgent notifications requiring immediate badge update:

```json
{
  "message": {
    "token": "device_token",
    "android": {
      "priority": "high",
      "data": {
        "badge": "1",
        "badge_action": "increment",
        "urgent": "true",
        "expiry": "300"
      }
    },
    "apns": {
      "headers": {
        "apns-priority": "10",
        "apns-push-type": "alert"
      },
      "payload": {
        "aps": {
          "badge": 1,
          "alert": {
            "title": "Urgent Alert",
            "body": "Immediate attention required"
          },
          "sound": "critical.wav"
        }
      }
    }
  }
}
```

### 4. Batch Badge Updates

For updating multiple badge-related notifications:

```json
{
  "messages": [
    {
      "token": "device_token_1",
      "data": {
        "badge": "5",
        "badge_action": "set"
      }
    },
    {
      "token": "device_token_2", 
      "data": {
        "badge": "2",
        "badge_action": "increment"  
      }
    },
    {
      "token": "device_token_3",
      "data": {
        "badge": "0",
        "badge_action": "clear"
      }
    }
  ]
}
```

## Testing and Validation

### Test Payload Examples

#### 1. Simple Test Badge
```json
{
  "message": {
    "token": "test_device_token",
    "data": {
      "badge": "1",
      "badge_action": "set",
      "test": "true"
    }
  }
}
```

#### 2. Badge Increment Test
```json
{
  "message": {
    "token": "test_device_token",
    "data": {
      "badge": "1",
      "badge_action": "increment",
      "test_scenario": "increment"
    }
  }
}
```

#### 3. Badge Clear Test
```json
{
  "message": {
    "token": "test_device_token", 
    "data": {
      "badge": "0",
      "badge_action": "clear",
      "test_scenario": "clear"
    }
  }
}
```

### Validation Checklist

#### iOS Payload Validation:
- ✅ `badge` field is integer (not string)
- ✅ `badge` value is non-negative
- ✅ `content-available: 1` for silent updates
- ✅ Total payload size < 4KB

#### Android Payload Validation:
- ✅ `badge` field is string in data section
- ✅ `badge_action` specified ("set", "increment", etc.)
- ✅ Data payload for background processing
- ✅ Total payload size reasonable

#### Cross-Platform Validation:
- ✅ Platform-specific sections properly formatted
- ✅ Badge values consistent across platforms
- ✅ Fallback handling for unsupported devices
- ✅ Proper error handling in app code

## Implementation Notes

### Server-Side Considerations

1. **User Context**: Calculate badge count based on user's actual unread items
2. **Device Tracking**: Maintain badge state per device for multi-device users
3. **Error Handling**: Handle failed deliveries and retry logic
4. **Rate Limiting**: Avoid excessive badge updates to preserve battery
5. **Analytics**: Track badge update delivery and user engagement

### App-Side Processing

The `notification_badge_plus` plugin automatically processes these payloads:

```dart
// Background message handler automatically called
static Future<void> handleBackgroundMessage(RemoteMessage message) async {
  final badgeData = message.data;
  
  if (badgeData.containsKey('badge')) {
    final badgeCount = int.tryParse(badgeData['badge'] ?? '0') ?? 0;
    final action = badgeData['badge_action'] ?? 'set';
    
    switch (action) {
      case 'set':
        await NotificationBadgePlus.setBadgeCount(badgeCount);
        break;
      case 'increment':
        await NotificationBadgePlus.incrementBadge();
        break;
      case 'decrement':
        await NotificationBadgePlus.decrementBadge();
        break;
      case 'clear':
        await NotificationBadgePlus.clearBadge();
        break;
    }
  }
}
```

## Common Issues and Solutions

### Issue 1: iOS Badge Not Updating in Background
**Solution**: Ensure `badge` field is in `aps` section and is integer type.

### Issue 2: Android Badge Not Showing
**Solution**: Use FCM data messages, not notification messages for background processing.

### Issue 3: Badge Count Sync Issues
**Solution**: Include actual unread count in payload, not just increment values.

### Issue 4: Cross-Platform Inconsistency
**Solution**: Use platform-specific payload sections with appropriate formatting.

This guide provides comprehensive payload structures for enabling automatic background badge updates with the `notification_badge_plus` plugin. Proper server-side implementation ensures seamless badge management across both iOS and Android platforms.