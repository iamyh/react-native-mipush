# Introduction

react-native-mipush,是对小米推送服务的封装，适用于ios和android。





# Install


git clone http://github.com/iamyh/react-native-mipush.git $YOUR_PROJECT/node_modules/

## IOS
* 在[dev.xiaomi.com](http://dev.xiaomi.com)将申请到的appid，appkey填写到对应的info.plist的MiSDKAppID，MiSDKAppKey。额外添加一个MiSDKRun，值为${MiSDKRun}。这个值是在Build Settings -> 点击+ -> Add User-Defined Setting中添加MiSDKRun，Debug的时候值为Debug，release的时候为Online。
* 引入RCTMiPush.xcodeproj.在项目下面的Libraries，右击Add Files To "YOUR PROJECT",选择node_modules/react-native-mipush的ios项目
* 添加lib,headers.在YOUR_PROJECT的General -> Linked Frameworks and Libraries 添加libxml2.dylib,libresolv.dylib,libz.dylib,libMiPushSDK.so,MobileCoreService.framework,SystemConfigration.framework.在YOUR_PROJECT的Build Settings -> Search Paths -> Header Search Path添加上$(SRCROOT)/../node_modules/react-native-mipush/ios recursive
* 修改Appdelegate.m.

```
#import "RCTMiPush.h"

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{  
  [RCTMiPush didFinishLaunchingWithOptions:launchOptions];
  return YES;
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
  [RCTMiPush didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification
{
  [RCTMiPush didReceiveRemoteNotification:notification];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
  [RCTMiPush didReceiveRemoteNotification:notification];
  completionHandler(UIBackgroundFetchResultNewData);
}

- (void)application:(UIApplication *) application didRegisterUserNotificationSettings:(nonnull UIUserNotificationSettings *)notificationSettings {
  if (notificationSettings) {
    NSMutableDictionary *setting = [[NSMutableDictionary alloc] init];
    NSString *typeStr = [NSString stringWithFormat:@"%lu",(unsigned long)notificationSettings.types];
    [setting setObject:typeStr forKey:@"type"];
    [RCTMiPush didRegisterUserNotificationSettings:setting];
  }
}

- (void)application:(UIApplication *)app didFailToRegisterForRemoteNotificationsWithError:(NSError *)err
{
  // 注册APNS失败
  [RCTMiPush didFailToRegisterForRemoteNotificationsWithError:err];
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;//清空角标
}
```

## Android
* 修改MiPushModule.java里面的APP_ID，APP_KEY(node_modules里面的java源文件)
* 修改MainActivity.java

```
import com.xiaomi.push.reactnative.MiPushPackage;

@Override
protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
            new MiPushPackage()
    );
}
    
修改项目的AndroidManifest.xml:
    <uses-permission android:name="android.permission.GET_TASKS" />
    <permission
        android:name="${YOUR_PACKAGE}.permission.MIPUSH_RECEIVE"
        android:protectionLevel="signature" />

    <uses-permission android:name="${YOUR_PACKAGE}.permission.MIPUSH_RECEIVE" />
    <uses-permission android:name="android.permission.VIBRATE" />
       
    <application
    <service
            android:name="com.xiaomi.push.service.XMJobService"
            android:enabled="true"
            android:exported="false"
            android:permission="android.permission.BIND_JOB_SERVICE"
            android:process=":pushservice" />

        <service
            android:name="com.xiaomi.push.service.XMPushService"
            android:enabled="true"
            android:process=":pushservice" />

        <service
            android:name="com.xiaomi.mipush.sdk.PushMessageHandler"
            android:enabled="true"
            android:exported="true" />
        <service
            android:name="com.xiaomi.mipush.sdk.MessageHandleService"
            android:enabled="true" />

        <receiver
            android:name="com.xiaomi.push.reactnative.MiPushMessageReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" />
            </intent-filter>
            <intent-filter>
                <action android:name="com.xiaomi.mipush.MESSAGE_ARRIVED" />
            </intent-filter>
            <intent-filter>
                <action android:name="com.xiaomi.mipush.ERROR" />
            </intent-filter>
        </receiver>
        <receiver
            android:name="com.xiaomi.push.service.receivers.NetworkStatusReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />

                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </receiver>
        <receiver
            android:name="com.xiaomi.push.service.receivers.PingReceiver"
            android:exported="false"
            android:process=":pushservice">
            <intent-filter>
                <action android:name="com.xiaomi.push.PING_TIMER" />
            </intent-filter>
        </receiver>

    </application>
```

* 往settings.gradle添加:

```
include ':react-native-mipush'
project(':react-native-mipush').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-mipush/android')
```

* 修改app/build.gradle里面的dependencies:

```
compile project(':react-native-mipush')
compile fileTree(dir: "libs", include: ["*.jar"])
```

## Server
参考官方的用法即可

# Usage
在app启动的js文件加上

```
import  MiPush from 'react-native-mipush';

class App extends Component {
	componentDidMount() {
		MiPush.registerMiPushAndConnect();
		this.pushlisteners = [
     		MiPush.addEventListener("mipush", this.onReceiveMessage.bind(this))
		];
	}
	onReceiveMessage(message) {
		//alert(messsage);所有的消息都会回调到这里
	}
}

在其他需要的地方：
import MiPush from 'react-native-mipush';

MiPush.setAlias(uid);
MiPush.unsetAlias(uid);
//更多的方法请参考index里面的类似写法
```

# License
under the MIT License
