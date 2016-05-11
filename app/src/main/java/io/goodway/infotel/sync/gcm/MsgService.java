package io.goodway.infotel.sync.gcm;

/**
 * Created by antoine on 5/11/16.
 */
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import io.goodway.infotel.R;
import io.goodway.infotel.activities.MainActivity;


public class MsgService extends IntentService {

    SharedPreferences prefs;
    NotificationCompat.Builder notification;
    NotificationManager manager;


    public MsgService() {
        super("MSGService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        prefs = getSharedPreferences("Chat", 0);

        MsgReceiver.completeWakefulIntent(intent);
    }




    private void sendNotification(String msg,String mobno,String name) {

        Bundle args = new Bundle();
        args.putString("mobno", mobno);
        args.putString("name", name);
        args.putString("msg", msg);
        Intent chat = new Intent(this, MainActivity.class);
        chat.putExtra("INFO", args);
        notification = new NotificationCompat.Builder(this);
        notification.setContentTitle(name);
        notification.setContentText(msg);
        notification.setTicker("New Message !");
        notification.setSmallIcon(R.mipmap.ic_launcher);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000,
                chat, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(contentIntent);
        notification.setAutoCancel(true);
        manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }


}