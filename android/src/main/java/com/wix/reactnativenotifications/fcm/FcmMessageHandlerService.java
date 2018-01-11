package com.wix.reactnativenotifications.fcm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wix.reactnativenotifications.core.InitialNotificationHolder;
import com.wix.reactnativenotifications.core.notifications.IntentExtras;
import com.wix.reactnativenotifications.core.notifications.NotificationProps;
import com.wix.reactnativenotifications.core.notifications.RemoteNotification;

import java.util.List;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class FcmMessageHandlerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(LOGTAG, "New message from firebase");
        String type = message.getData().get("key1");
        if(type!=null && type.equalsIgnoreCase("call")){
            try {
                // The desired behavior upon notification opening is as follows:
                // - If app is in foreground (and possibly has several activities in stack), simply keep it as-is in foreground.
                // - If app is in background, bring it to foreground as-is (context stack untampered).
                //   A distinction is made in this case such that if app went to back due to *back-button*, is should be recreated (this
                //   is Android's native behavior).
                // - If app is dead, launch it through the main context (as Android launchers do).
                // Overall, THIS IS EXACTLY THE SAME AS ANDROID LAUNCHERS WORK. So, we use the same configuration (action, categories and
                // flags) as they do.
                if(this.isBackground()){
                    Log.d(LOGTAG, "App in Background");
                    Context appContext = getApplicationContext();
                    final Intent helperIntent = appContext.getPackageManager().getLaunchIntentForPackage(appContext.getPackageName());
                    final Intent intent = new Intent(appContext, Class.forName(helperIntent.getComponent().getClassName()));
                    final NotificationProps notification = InitialNotificationHolder.getInstance().get();

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                    intent.putExtra(IntentExtras.FCM_PREFIX, true);
                    if (notification != null) {
                        // If an initial notification has been set from a cold boot, we must pass on
                        // the notification to ensure it is accessible from subsequent getInitialNotification calls
                        intent.putExtras(notification.asBundle());

                    }
                    appContext.startActivity(intent);
                } else {
                    Log.d(LOGTAG, "App in Foreground");
                }
            } catch (ClassNotFoundException e) {
                // Note: this is an imaginary scenario cause we're asking for a class of our very own package.
                Log.e(LOGTAG, "Failed to launch/resume app", e);
            }
        }

        final NotificationProps notificationProps = NotificationProps.fromRemoteMessage(this, message);
        new RemoteNotification(this, notificationProps).onReceived();
    }

    public boolean isBackground() {
        Context context = this.getApplicationContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                Log.v("NotificationTest pro", appProcess.processName);
                Log.v(" NotificationTest pkg", context.getPackageName());
                System.out.println("NotificationTest -BIN" + appProcess.importance);
                System.out.println("NotificationTest -BCG" + ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND);

                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND ||
                        appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                    Log.v("NotificationTest -B", appProcess.processName);
                    return true;
                }else{
                    Log.i("NotificationTest -F", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }
}
