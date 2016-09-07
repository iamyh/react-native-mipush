package com.xiaomi.push.reactnative;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

public  class MiPushMessageReceiver extends PushMessageReceiver {

    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        Log.d(MiPushModule.TAG, "onReceivePassThroughMessage is called. " + message.toString());

        if (message.getTopic() != null) {
            mTopic = message.getTopic();
        }
        else {
            Log.d(MiPushModule.TAG, "getTopic is null.");
        }

        if (message.getAlias() != null) {
            mAlias = message.getAlias();
        }
        else {
            Log.d(MiPushModule.TAG, "getAlias is null.");
        }

        Bundle bundle = new Bundle();
        bundle.putString("type", MiPushModule.MiPush_didReceivePassThroughMessage);
        bundle.putString("data", message.toString());
        MiPushModule.sendEvent(bundle);
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        Log.d(MiPushModule.TAG, "onNotificationMessageClicked is called. " + message.toString());

        if (message.getTopic() != null) {
            mTopic = message.getTopic();
        }
        else {
            Log.d(MiPushModule.TAG, "getTopic is null.");
        }

        if (message.getAlias() != null) {
            mAlias = message.getAlias();
        }
        else {
            Log.d(MiPushModule.TAG, "getAlias is null.");
        }

        Bundle bundle = new Bundle();
        bundle.putString("type", MiPushModule.MiPush_didNotificationMessageClicked);
        bundle.putString("data", message.toString());
        MiPushModule.sendEvent(bundle);
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        Log.d(MiPushModule.TAG, "onNotificationMessageArrived is called. " + message.toString());

        if (message.getTopic() != null) {
            mTopic = message.getTopic();
        }
        else {
            Log.d(MiPushModule.TAG, "getTopic is null.");
        }

        if (message.getAlias() != null) {
            mAlias = message.getAlias();
        }
        else {
            Log.d(MiPushModule.TAG, "getAlias is null.");
        }

        Bundle bundle = new Bundle();
        bundle.putString("type", MiPushModule.MiPush_didReceiveRemoteNotification);
        bundle.putString("data", message.toString());
        MiPushModule.sendEvent(bundle);

    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        Log.d(MiPushModule.TAG, "onCommandResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String errMsg = "";
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
            } else {
                errMsg = "command register error";
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;

            } else {
                errMsg = "command set alias error";
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;

            } else {
                errMsg = "command unset alias error";
            }
        } else if (MiPushClient.COMMAND_SET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;

            } else {
                errMsg = "command set account error";
            }
        } else if (MiPushClient.COMMAND_UNSET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;

            } else {
                errMsg = "command unset account error";
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;

            } else {
                errMsg = "command subscribe topic error";
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;

            } else {
                errMsg = "command subscribe topic error";
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;

            } else {
                errMsg = "command set accept time error";
            }
        } else {
            errMsg = "command not match error.comand=" + command;
        }

        if (!errMsg.equals("")) {
            Log.e(MiPushModule.TAG, "onCommandResult:errMsg=" + errMsg);
        }

        Bundle bundle = new Bundle();
        bundle.putString("command", command);
        if (!errMsg.equals("")) {
            bundle.putString("error", errMsg);
            bundle.putString("type", MiPushModule.MiPush_requestErrWithSelector);
        }
        else {
            bundle.putString("type", MiPushModule.MiPush_requestSuccWithSelector);
        }

        bundle.putString("data", message.toString());
        MiPushModule.sendEvent(bundle);
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        Log.d(MiPushModule.TAG, "onReceiveRegisterResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;

            } else {
                Log.e(MiPushModule.TAG, "onReceiveRegisterResult register error");
                Bundle bundle = new Bundle();
                bundle.putString("error", message.toString());
                bundle.putString("type", MiPushModule.MiPush_didFailToRegisterForRemoteNotificationsWithError);
                MiPushModule.sendEvent(bundle);
                return;
            }
        } else {
            Log.e(MiPushModule.TAG, "command not match error.command=" + command);
        }

        Bundle bundle = new Bundle();
        bundle.putString("data", message.toString());
        bundle.putString("type", MiPushModule.MiPush_didRegisterForRemoteNotificationsWithDeviceToken);
        MiPushModule.sendEvent(bundle);

    }
}
