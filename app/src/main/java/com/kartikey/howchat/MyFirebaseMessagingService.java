package com.kartikey.howchat;

import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("Recevied", "notification received");

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();

        String from_user_id = remoteMessage.getData().get("from_user_id");

        Log.d("Recevied", notification_title);
        Log.d("Recevied", notification_message);
        Log.d("Recevied", click_action);
        Log.d("Recevied", from_user_id);



        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "Friend_req")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_message);

     Intent  resultIntent = new Intent(click_action);
            resultIntent.putExtra("userId", from_user_id);


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this,0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent)
        .setAutoCancel(true);

        int mNotificationID =(int) System.currentTimeMillis();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationID, mBuilder.build());


    }
}
