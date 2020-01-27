
#import <UIKit/UIKit.h>
#import <PushKit/PushKit.h>
#import "RNNotifications.h"
#import "RNNotificationCenterListener.h"
#import "RNPushKit.h"
#import "RNNotificationCenterMulticast.h"

@implementation RNNotifications {
    RNPushKit* _pushKit;
    RNNotificationCenterListener* _notificationCenterListener;
    RNNotificationEventHandler* _notificationEventHandler;
    RNPushKitEventHandler* _pushKitEventHandler;
    RNEventEmitter* _eventEmitter;
    RNNotificationCenterMulticast* _notificationCenterMulticast;
}

- (instancetype)init {
    self = [super init];
    _notificationEventHandler = [[RNNotificationEventHandler alloc] initWithStore:[RNNotificationsStore new]];
    return self;
}

+ (instancetype)sharedInstance {
    static RNNotifications *sharedInstance = nil;
    static dispatch_once_t onceToken;
    
    dispatch_once(&onceToken, ^{
        sharedInstance = [[RNNotifications alloc] init];
    });
    return sharedInstance;
}

+ (void)startMonitorNotifications {
    [[self sharedInstance] startMonitorNotifications];
}

+ (void)startMonitorPushKitNotifications {
    [[self sharedInstance] startMonitorPushKitNotifications];
}

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(id)deviceToken {
    [[self sharedInstance] didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

+ (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [[self sharedInstance] didFailToRegisterForRemoteNotificationsWithError:error];
}

+ (void)addNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [[self sharedInstance] addNativeDelegate:delegate];
}

+ (void)removeNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [[self sharedInstance] removeNativeDelegate:delegate];
}

- (void)startMonitorNotifications {
    _notificationCenterListener = [[RNNotificationCenterListener alloc] initWithNotificationEventHandler:_notificationEventHandler];
    
    _notificationCenterMulticast = [[RNNotificationCenterMulticast alloc] init];
    [[UNUserNotificationCenter currentNotificationCenter] setDelegate:_notificationCenterMulticast];
    
    [_notificationCenterMulticast addNativeDelegate:_notificationCenterListener];
}

- (void)startMonitorPushKitNotifications {
    _pushKitEventHandler = [RNPushKitEventHandler new];
    _pushKit = [[RNPushKit alloc] initWithEventHandler:_pushKitEventHandler];
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(id)deviceToken {
    [_notificationEventHandler didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [_notificationEventHandler didFailToRegisterForRemoteNotificationsWithError:error];
}

- (void)addNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [_notificationCenterMulticast addNativeDelegate:delegate];
}

- (void)removeNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [_notificationCenterMulticast removeNativeDelegate:delegate];
}

RCT_EXPORT_METHOD(checkPermissions:(RCTPromiseResolveBlock) resolve
                  reject:(RCTPromiseRejectBlock) reject) {
    UIUserNotificationSettings *currentSettings = [[UIApplication sharedApplication] currentUserNotificationSettings];
    resolve(@{
              @"badge": @((currentSettings.types & UIUserNotificationTypeBadge) > 0),
              @"sound": @((currentSettings.types & UIUserNotificationTypeSound) > 0),
              @"alert": @((currentSettings.types & UIUserNotificationTypeAlert) > 0),
              });
}

RCT_EXPORT_METHOD(setApplicationIconBadgeNumber:(NSInteger)number)
{
  [RCTSharedApplication() setApplicationIconBadgeNumber: number];
}

#if !TARGET_OS_TV

RCT_EXPORT_METHOD(removeAllDeliveredNotifications)
{
  if ([UNUserNotificationCenter class]) {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center removeAllDeliveredNotifications];
  }
}

RCT_EXPORT_METHOD(removeDeliveredNotifications:(NSArray<NSString *> *)identifiers)
{
  if ([UNUserNotificationCenter class]) {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center removeDeliveredNotificationsWithIdentifiers:identifiers];
  }
}

RCT_EXPORT_METHOD(getDeliveredNotifications:(RCTResponseSenderBlock)callback)
{
  if ([UNUserNotificationCenter class]) {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center getDeliveredNotificationsWithCompletionHandler:^(NSArray<UNNotification *> * _Nonnull notifications) {
      NSMutableArray<NSDictionary *> *formattedNotifications = [NSMutableArray new];

      for (UNNotification *notification in notifications) {
        [formattedNotifications addObject:RCTFormatUNNotification(notification)];
      }
      callback(@[formattedNotifications]);
    }];
  }
}

#endif !TARGET_OS_TV

@end