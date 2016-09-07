package com.xiaomi.push.reactnative;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.BuildConfig;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

/**
 * Created by lvbingru on 10/23/15.
 */
public class MiPushModule extends ReactContextBaseJavaModule {

    private Intent mIntent;
    private static Boolean registered = false;
    private static MiPushModule gModules = null;
    private static String holdMessage = null;
    private static String APP_ID = "2882303761517499178";
    private static String APP_KEY = "5111749999178";

    private static String deviceId = null;

    public MiPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        if (!registered) {
            Log.d(TAG, "register");
            MiPushClient.registerPush(reactContext, APP_ID, APP_KEY);
            registered = true;
            gModules = this;
            try {
                TelephonyManager telephonyManager = (TelephonyManager)reactContext.getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = telephonyManager.getDeviceId();
            } catch (Exception e) {
                deviceId = "unknow";
            }

        }
    }

    @Override
        public String getName() {
            return "MiPush";
        }

    @Override
        public Map<String, Object> getConstants() {
            final Map<String, Object> constants = new HashMap<>();
            if (holdMessage != null) {
                constants.put("initialNotification", holdMessage);
            }
            return constants;
        }

    @Override
        public void initialize() {
            super.initialize();
            gModules = this;
        }

    @Override
        public void onCatalystInstanceDestroy() {
            super.onCatalystInstanceDestroy();
            gModules = null;
        }

    public static void sendEvent(Bundle bundle) {
        if (gModules != null){
            bundle.putString("device_id", deviceId);
            WritableMap message = Arguments.fromBundle(bundle);
            Log.d(TAG, message.toString());
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = gModules.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit("mipush", message);
            return;
        }
        else {
            Log.e(TAG, "sendEvent gModules is null.");
        }
    }

    public static final String TAG = "MPushReceiver";
    public static final String MiPush_didFinishLaunchingWithOptions = "MiPush_didFinishLaunchingWithOptions";
    public static final String MiPush_didRegisterUserNotificationSettings = "MiPush_didRegisterUserNotificationSettings";
    public static final String MiPush_didFailToRegisterForRemoteNotificationsWithError = "MiPush_didFailToRegisterForRemoteNotificationsWithError";
    public static final String MiPush_didRegisterForRemoteNotificationsWithDeviceToken = "MiPush_didRegisterForRemoteNotificationsWithDeviceToken";
    public static final String MiPush_didReceiveRemoteNotification = "MiPush_didReceiveRemoteNotification";
    public static final String MiPush_didNotificationMessageClicked = "MiPush_didNotificationMessageClicked";
    public static final String MiPush_didCommandResult = "MiPush_didCommandResult";
    public static final String MiPush_didReceivePassThroughMessage = "MiPush_didReceivePassThroughMessage";
    public static final String MiPush_didReceiveLocalNotification = "MiPush_didReceiveLocalNotification";
    public static final String MiPush_requestSuccWithSelector = "MiPush_requestSuccWithSelector";
    public static final String MiPush_requestErrWithSelector = "MiPush_requestErrWithSelector";

    @ReactMethod
        public void getInitialMessage(Promise promise) {
            WritableMap params = MiPushHelper.getDataOfIntent(mIntent);
            // Add missing NOTIFICATION_MESSAGE_CLICKED message in PushMessageReceiver
            if (params != null) {
                params.putString("type", "NOTIFICATION_MESSAGE_CLICKED");
            }
            promise.resolve(params);
        }

    @ReactMethod
        public void setAlias(String alias) {
            MiPushClient.setAlias(getReactApplicationContext(), alias, "");
        }

    @ReactMethod
        public void unsetAlias(String alias) {
            MiPushClient.unsetAlias(getReactApplicationContext(), alias, "");
        }

    @ReactMethod
        public void setUserAccount(String userAccount) {
            MiPushClient.setUserAccount(getReactApplicationContext(), userAccount, "");
        }

    @ReactMethod
        public void unsetUserAccount(String userAccount) {
            MiPushClient.unsetUserAccount(getReactApplicationContext(), userAccount, "");
        }

    @ReactMethod
        public void subscribe(String topic) {
            MiPushClient.subscribe(getReactApplicationContext(), topic, "");
        }

    @ReactMethod
        public void unsubscribe(String topic) {
            MiPushClient.unsubscribe(getReactApplicationContext(), topic, "");
        }

    @ReactMethod
        public void pausePush(String category) {
            MiPushClient.pausePush(getReactApplicationContext(), category);
        }

    @ReactMethod
        public void resumePush(String category) {
            MiPushClient.resumePush(getReactApplicationContext(), category);
        }

    @ReactMethod
        public void setAcceptTime(int startHour, int startMin, int endHour, int endMin, String category) {
            MiPushClient.setAcceptTime(getReactApplicationContext(), startHour, startMin, endHour, endMin, category);
        }

    @ReactMethod
        public void getAllAlias(Promise promise) {
            List<String> allAlias = MiPushClient.getAllAlias(getReactApplicationContext());
            String[] allAliasArray = allAlias.toArray(new String[allAlias.size()]);
            promise.resolve(Arguments.fromArray(allAliasArray));
        }

    @ReactMethod
        public void getAllTopics(Promise promise) {
            List<String> allTopics = MiPushClient.getAllAlias(getReactApplicationContext());
            String[] allTopicsArray = allTopics.toArray(new String[allTopics.size()]);
            promise.resolve(Arguments.fromArray(allTopicsArray));
        }

    @ReactMethod
        public void reportMessageClicked(String msgId) {
            MiPushClient.reportMessageClicked(getReactApplicationContext(), msgId);
        }

    @ReactMethod
        public void clearNotification(int notifyId) {
            MiPushClient.clearNotification(getReactApplicationContext(), notifyId);
        }

    @ReactMethod
        public void clearAllNotification() {
            MiPushClient.clearNotification(getReactApplicationContext());
        }

    @ReactMethod
        public void setLocalNotificationType(int notifyType) {
            MiPushClient.setLocalNotificationType(getReactApplicationContext(), notifyType);
        }

    @ReactMethod
        public void clearLocalNotificationType() {
            MiPushClient.clearLocalNotificationType(getReactApplicationContext());
        }

    @ReactMethod
        public void getRegId(Promise promise) {
            MiPushClient.getRegId(getReactApplicationContext());
        }

    private Set _stringArrayToSet(ReadableArray array) {
        Set<String> set = new HashSet<String>();
        if (array != null) {
            int size = array.size();
            for (int i=0;i<size;i++) {
                String obj = array.getString(i);
                set.add(obj);
            }
        }
        return set;
    }
    private Set _intArrayToSet(ReadableArray array) {
        Set<Integer> set = new HashSet<Integer>();
        if (array != null) {
            int size = array.size();
            for (int i=0;i<size;i++) {
                Integer obj = array.getInt(i);
                set.add(obj);
            }
        }
        return set;
    }
}
