package com.wix.reactnativenotifications.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wix.reactnativenotifications.core.notifications.NotificationProps;
import com.wix.reactnativenotifications.core.notifications.RemoteNotification;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class FcmMessageHandlerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
      try {
            // The desired behavior upon notification opening is as follows:
            // - If app is in foreground (and possibly has several activities in stack), simply keep it as-is in foreground.
            // - If app is in background, bring it to foreground as-is (context stack untampered).
            //   A distinction is made in this case such that if app went to back due to *back-button*, is should be recreated (this
            //   is Android's native behavior).
            // - If app is dead, launch it through the main context (as Android launchers do).
            // Overall, THIS IS EXACTLY THE SAME AS ANDROID LAUNCHERS WORK. So, we use the same configuration (action, categories and
            // flags) as they do.
            Context appContext = getApplicationContext();
            final Intent helperIntent = appContext.getPackageManager().getLaunchIntentForPackage(appContext.getPackageName());
            final Intent intent = new Intent(appContext, Class.forName(helperIntent.getComponent().getClassName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            // Note: this is an imaginary scenario cause we're asking for a class of our very own package.
            Log.e(LOGTAG, "Failed to launch/resume app", e);
        }
        Log.d(LOGTAG, "New message from firebase");
        final NotificationProps notificationProps = NotificationProps.fromRemoteMessage(this, message);
        new RemoteNotification(this, notificationProps).onReceived();
    }
}
