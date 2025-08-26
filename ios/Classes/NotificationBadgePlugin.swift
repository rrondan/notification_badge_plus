import Flutter
import UIKit
import UserNotifications

public class NotificationBadgePlugin: NSObject, FlutterPlugin {
    private var currentBadgeCount: Int = 0
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "notification_badge", binaryMessenger: registrar.messenger())
        let instance = NotificationBadgePlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
        
        // Register for application lifecycle notifications
        NotificationCenter.default.addObserver(
            instance,
            selector: #selector(applicationDidBecomeActive),
            name: UIApplication.didBecomeActiveNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            instance,
            selector: #selector(applicationDidEnterBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "setBadgeCount":
            setBadgeCount(call: call, result: result)
        case "getBadgeCount":
            getBadgeCount(result: result)
        case "isSupported":
            result(true) // iOS always supports badges
        case "getDeviceManufacturer":
            result("Apple")
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func setBadgeCount(call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let args = call.arguments as? [String: Any],
              let count = args["count"] as? Int else {
            result(FlutterError(code: "INVALID_ARGUMENT", message: "Invalid count argument", details: nil))
            return
        }
        
        if count < 0 {
            result(FlutterError(code: "INVALID_ARGUMENT", message: "Badge count cannot be negative", details: nil))
            return
        }
        
        self.currentBadgeCount = count
        
        // Set the badge count on the main thread
        DispatchQueue.main.async {
            if #available(iOS 16.0, *) {
                // iOS 16+ method
                UNUserNotificationCenter.current().setBadgeCount(count) { error in
                    if let error = error {
                        print("Error setting badge count: \(error.localizedDescription)")
                        result(false)
                    } else {
                        result(true)
                    }
                }
            } else {
                // Legacy method for iOS 15 and earlier
                UIApplication.shared.applicationIconBadgeNumber = count
                result(true)
            }
        }
    }
    
    private func getBadgeCount(result: @escaping FlutterResult) {
        if #available(iOS 16.0, *) {
            UNUserNotificationCenter.current().getBadgeCount { count in
                DispatchQueue.main.async {
                    result(Int(count))
                }
            }
        } else {
            DispatchQueue.main.async {
                result(UIApplication.shared.applicationIconBadgeNumber)
            }
        }
    }
    
    @objc private func applicationDidBecomeActive() {
        // App became active - sync badge count if needed
        DispatchQueue.main.async {
            if #available(iOS 16.0, *) {
                UNUserNotificationCenter.current().getBadgeCount { count in
                    self.currentBadgeCount = Int(count)
                }
            } else {
                self.currentBadgeCount = UIApplication.shared.applicationIconBadgeNumber
            }
        }
    }
    
    @objc private func applicationDidEnterBackground() {
        // App entered background - ensure badge count is preserved
        if currentBadgeCount > 0 {
            DispatchQueue.main.async {
                if #available(iOS 16.0, *) {
                    UNUserNotificationCenter.current().setBadgeCount(self.currentBadgeCount) { error in
                        if let error = error {
                            print("Error preserving badge count in background: \(error.localizedDescription)")
                        }
                    }
                } else {
                    UIApplication.shared.applicationIconBadgeNumber = self.currentBadgeCount
                }
            }
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}