package com.wix.reactnativenotifications.fcm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotification;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

private static final String LAUNCH_FLAG_KEY_NAME = "launchedFromNotification";

/**
 * Instance-ID + token refreshing handling service. Contacts the FCM to fetch the updated token.
 *
 * @author amitd
 */
public class FcmInstanceIdListenerService extends FirebaseMessagingService {
  
    protected static boolean flag = false;
    
    @Override
    public void onMessageReceived(RemoteMessage message){
        Bundle bundle = message.toIntent().getExtras();
        String type = message.getData().get("key1");
        if(type!=null && type.equalsIgnoreCase("call")){
          try {
            if(this.isBackground()){
              Context.appContext = getApplicationContext();
              final Intent helperIntent = appContext.getPackageManager().getLaunchIntentForPackage(appContext.getPackageName());
              final Intent intent = new Intent(appContext, Class.forName(helperIntent.getComponent().getClassName()));
              
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
              intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
              
              intent.putExtra(LAUNCH_FLAG_KEY_NAME, true);
              appContent.startActivity(intent);
            }
          } catch (ClassNotFoundException e){
            Log.d(LOGTAG, "Failed to launch/resume app", e);
          }
        } else {
          if(this.isBackground() && !flag){
            ShortcutBadger.applyCount(getApplicationContext(), 1);
            flag = true;
          }
        }


        Log.d(LOGTAG, "New message from FCM: " + bundle);
        try {
            final IPushNotification notification = PushNotification.get(getApplicationContext(), bundle);
            notification.onReceived();
        } catch (IPushNotification.InvalidNotificationException e) {
            // An FCM message, yes - but not the kind we know how to work with.
            Log.v(LOGTAG, "FCM message handling aborted", e);
        }
    }
  
  public boolean isBackground(){
      Context context = this.getApplicationContext();
      ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
      for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
          if (appProcess.processName.equals(context.getPackageName())) {
              if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND ||
                  appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                  return true;
              } else {
                  return false;
              }
          }
      }
      return false;
  }
}
