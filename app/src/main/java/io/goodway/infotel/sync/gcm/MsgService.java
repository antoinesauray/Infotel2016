package io.goodway.infotel.sync.gcm;

/**
 * Created by antoine on 5/11/16.
 */
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmReceiver;


public class MsgService extends IntentService {

    SharedPreferences prefs;
    NotificationCompat.Builder notification;
    NotificationManager manager;


    public MsgService() {
        super("MSGService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        //if(intent.)
        GcmReceiver.completeWakefulIntent(intent);
    }

}