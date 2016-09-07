#import "RCTMiPush.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import "RCTConvert.h"
#import "RCTUtils.h"
#import <AdSupport/ASIdentifierManager.h> 

NSString *const MiPush_didFinishLaunchingWithOptions = @"MiPush_didFinishLaunchingWithOptions";
NSString *const MiPush_didRegisterUserNotificationSettings = @"MiPush_didRegisterUserNotificationSettings";
NSString *const MiPush_didFailToRegisterForRemoteNotificationsWithError = @"MiPush_didFailToRegisterForRemoteNotificationsWithError";
NSString *const MiPush_didRegisterForRemoteNotificationsWithDeviceToken = @"MiPush_didRegisterForRemoteNotificationsWithDeviceToken";
NSString *const MiPush_didReceiveRemoteNotification = @"MiPush_didReceiveRemoteNotification";
NSString *const MiPush_didReceiveLocalNotification = @"MiPush_didReceiveLocalNotification";
NSString *const MiPush_requestSuccWithSelector = @"MiPush_requestSuccWithSelector";
NSString *const MiPush_requestErrWithSelector = @"MiPush_requestErrWithSelector";

@implementation RCTMiPush

+ (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)err
{
    NSLog(@"%s", __PRETTY_FUNCTION__);
    [[NSNotificationCenter defaultCenter] postNotificationName:MiPush_didFailToRegisterForRemoteNotificationsWithError
                                                        object:self
                                                      userInfo:[err localizedDescription]];
}
+ (void)didRegisterUserNotificationSettings:(NSDictionary *)notificationSettings
{
    if ([UIApplication instancesRespondToSelector:@selector(registerForRemoteNotifications)]) {
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    
    [[NSNotificationCenter defaultCenter] postNotificationName:MiPush_didRegisterUserNotificationSettings
                                                        object:self
                                                      userInfo:notificationSettings];
}
// Required for the register event.
+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSLog(@"%s", __PRETTY_FUNCTION__);
    [MiPushSDK bindDeviceToken:deviceToken];
    
    NSMutableString *hexString = [NSMutableString string];
    NSUInteger deviceTokenLength = deviceToken.length;
    const unsigned char *bytes = deviceToken.bytes;
    for (NSUInteger i = 0; i < deviceTokenLength; i++) {
        [hexString appendFormat:@"%02x", bytes[i]];
    }

    NSDictionary *info = [[NSDictionary alloc] initWithObjectsAndKeys:hexString, @"device_token", nil];
    [[NSNotificationCenter defaultCenter] postNotificationName:MiPush_didRegisterForRemoteNotificationsWithDeviceToken
                                                        object:self
                                                      userInfo:info];
}
// Required for the notification event.
+ (void)didReceiveRemoteNotification:(NSDictionary *)notification
{
    NSLog(@"%s", __PRETTY_FUNCTION__);
    NSString *messageId = [notification objectForKey:@"_id_"];
    if (messageId != nil) {
        [MiPushSDK openAppNotify:messageId];
    }

    // 针对长连接做了消息排重合并，只在下面处理即可
    [MiPushSDK handleReceiveRemoteNotification: notification];

    [[NSNotificationCenter defaultCenter] postNotificationName:MiPush_didReceiveRemoteNotification
                                                        object:self
                                                      userInfo:notification];
}
// Required for the localNotification event.
+ (void)didReceiveLocalNotification:(UILocalNotification *)notification
{
    NSLog(@"%s", __PRETTY_FUNCTION__);
    NSMutableDictionary *details = [NSMutableDictionary new];
    if (notification.alertBody) {
        details[@"alertBody"] = notification.alertBody;
    }
    if (notification.userInfo) {
        details[@"userInfo"] = RCTJSONClean(notification.userInfo);
    }

    [[NSNotificationCenter defaultCenter] postNotificationName:MiPush_didReceiveLocalNotification
                                                        object:self
                                                      userInfo:details];
}

+ (void)didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    NSLog(@"%s", __PRETTY_FUNCTION__);
    NSMutableDictionary *initialProperties = [[NSMutableDictionary alloc] init];
    NSDictionary *remoteNotification = launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey];

    if (remoteNotification) {
        NSString *messageId = [remoteNotification objectForKey:@"_id_"];
        if (messageId != nil) {
            [MiPushSDK openAppNotify:messageId];
        }
        
        [initialProperties setObject:remoteNotification forKey:@"remoteNotification"];
        [[NSNotificationCenter defaultCenter] postNotificationName:MiPush_didFinishLaunchingWithOptions
                                                            object:self
                                                          userInfo:initialProperties];
    }

}

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;
@synthesize methodQueue = _methodQueue;

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)setBridge:(RCTBridge *)bridge
{
    _bridge = bridge;

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleFailToRegisterForRemoteNotificationsWithError:)
                                                 name:MiPush_didFailToRegisterForRemoteNotificationsWithError
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleRegisterForRemoteNotificationsWithDeviceToken:)
                                                 name:MiPush_didRegisterForRemoteNotificationsWithDeviceToken
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleReceiveRemoteNotification:)
                                                 name:MiPush_didReceiveRemoteNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleReceiveLocalNotification:)
                                                 name:MiPush_didReceiveLocalNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleRegisterUserNotificationSettings:)
                                                 name:MiPush_didRegisterUserNotificationSettings
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleFinishLaunchingWithOptions:)
                                                 name:MiPush_didFinishLaunchingWithOptions
                                               object:nil];
}

- (void)handleFailToRegisterForRemoteNotificationsWithError:(NSNotification *)notification
{
    NSLog(@"%s,notification=%@", __PRETTY_FUNCTION__, notification);
    [self sendMiPushEvent:@{
                            @"type": @"REGISTER_REMOTE_NOTIFICATION_FAILED",
                            @"error": notification.userInfo
                            }];
}

- (void)handleFinishLaunchingWithOptions:(NSNotification *)notification
{
    NSLog(@"%s,notification=%@", __PRETTY_FUNCTION__, notification);
    if (notification) {
        [self sendMiPushEvent:@{
                                @"type": MiPush_didFinishLaunchingWithOptions,
                                @"data": notification.userInfo
                                }];
    }
}
- (void)handleRegisterUserNotificationSettings:(NSNotification *)notification
{
    NSLog(@"%s,notification=%@", __PRETTY_FUNCTION__, notification);
    
    [self sendMiPushEvent:@{
                            @"type": MiPush_didRegisterUserNotificationSettings,
                            @"data": notification.userInfo
                            }];
}

- (void)handleRegisterForRemoteNotificationsWithDeviceToken:(NSNotification *)notification
{
    NSLog(@"%s,notification=%@", __PRETTY_FUNCTION__, notification);
    
    [self sendMiPushEvent:@{
        @"type": MiPush_didRegisterForRemoteNotificationsWithDeviceToken,
        @"data": notification.userInfo
    }];
}

- (void)handleReceiveRemoteNotification:(NSNotification *)notification
{
    NSLog(@"%s,notification=%@", __PRETTY_FUNCTION__, notification);
    
    [self sendMiPushEvent:@{
        @"type": MiPush_didReceiveRemoteNotification,
        @"data": notification.userInfo
    }];
}

- (void)handleReceiveLocalNotification:(NSNotification *)notification
{
    [self sendMiPushEvent:@{
        @"type": MiPush_didReceiveLocalNotification,
        @"data": notification.userInfo
    }];
}

- (void)miPushRequestSuccWithSelector:(NSString *)selector data:(NSDictionary *)data
{
    // 请求成功
    NSLog(@"%s selector=%@,data=%@", __PRETTY_FUNCTION__, selector, data);
    
    [self sendMiPushEvent:@{
        @"type": MiPush_requestSuccWithSelector,
        @"data": data,
        @"command":selector
    }];
}

- (void)miPushRequestErrWithSelector:(NSString *)selector error:(int)error data:(NSDictionary *)data
{
    NSLog(@"%s selector=%@,data=%@", __PRETTY_FUNCTION__, selector, data);
    if (!data) {
        data = @{};
    }

    [self sendMiPushEvent:@{
        @"type": MiPush_requestErrWithSelector,
        @"command": selector,
        @"error": @(error),
        @"data": data
    }];
}

- (void)miPushReceiveNotification:( NSDictionary *)data
{
    
    NSLog(@"%s,data=%@", __PRETTY_FUNCTION__, data);
    // 长连接收到的消息。消息格式跟APNs格式一样
    [self sendMiPushEvent:@{
        @"type": MiPush_didReceiveRemoteNotification,
        @"data": data
    }];
}

- (void)sendMiPushEvent:(id)body
{
    NSDictionary *info = body;
    NSMutableDictionary *newInfo = [[NSMutableDictionary alloc] initWithDictionary:info];
    dispatch_async(_methodQueue, ^{
        [_bridge.eventDispatcher sendDeviceEventWithName:@"mipush" body:newInfo];
    });
}

RCT_EXPORT_METHOD(registerMiPush)
{
    [MiPushSDK registerMiPush:self];
}

RCT_EXPORT_METHOD(registerMiPushWithType:(int)type)
{
    [MiPushSDK registerMiPush:self type:(UIRemoteNotificationType)type];
}

RCT_EXPORT_METHOD(registerMiPushAndConnect:(BOOL)isConnect type:(int)type)
{
    [MiPushSDK registerMiPush:self type:(UIRemoteNotificationType)type connect:isConnect];
}

RCT_EXPORT_METHOD(unregisterMiPush)
{
    [MiPushSDK unregisterMiPush];
}

RCT_EXPORT_METHOD(bindDeviceToken:(NSString *)hexDeviceToken)
{
    NSMutableData * deviceToken = [[NSMutableData alloc] init];
    char bytes[3] = {'\0', '\0', '\0'};
    for (int i=0; i<[hexDeviceToken length] / 2; i++) {
        bytes[0] = [hexDeviceToken characterAtIndex:i*2];
        bytes[1] = [hexDeviceToken characterAtIndex:i*2+1];
        unsigned char c = strtol(bytes, NULL, 16);
        [deviceToken appendBytes:&c length:1];
    }
    
    [MiPushSDK bindDeviceToken:deviceToken];
}

RCT_EXPORT_METHOD(setAlias:(NSString *)alias)
{
    [MiPushSDK setAlias:alias];
}

RCT_EXPORT_METHOD(unsetAlias:(NSString *)alias)
{
    [MiPushSDK unsetAlias:alias];
}

RCT_EXPORT_METHOD(setAccount:(NSString *)account)
{
    [MiPushSDK setAccount:account];
}

RCT_EXPORT_METHOD(unsetAccount:(NSString *)account)
{
    [MiPushSDK unsetAccount:account];
}

RCT_EXPORT_METHOD(subscribe:(NSString *)topic)
{
    [MiPushSDK subscribe:topic];
}

RCT_EXPORT_METHOD(unsubscribe:(NSString *)topic)
{
    [MiPushSDK unsubscribe:topic];
}

RCT_EXPORT_METHOD(openAppNotify:(NSString *)messageId)
{
    [MiPushSDK openAppNotify:messageId];
}

RCT_EXPORT_METHOD(getAllAliasAsync)
{
    [MiPushSDK getAllAliasAsync];
}

RCT_EXPORT_METHOD(getAllAccountAsync)
{
    [MiPushSDK getAllAccountAsync];
}

RCT_EXPORT_METHOD(getAllTopicAsync)
{
    [MiPushSDK getAllTopicAsync];
}

RCT_EXPORT_METHOD(getSDKVersion:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString * sdkVersion = [MiPushSDK getSDKVersion];

    resolve(sdkVersion);
}

RCT_EXPORT_METHOD(isOpen:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    
    NSString *result = @"open";
    
    UIUserNotificationSettings * settings = [[UIApplication sharedApplication] currentUserNotificationSettings];
    if (settings.types == UIUserNotificationTypeNone) {
        result = @"close";
    }
    
    resolve(result);
}


@end
