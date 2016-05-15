package io.goodway.infotel.sync.gcm;

/**
 * Created by antoine on 5/13/16.
 */
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import io.goodway.infotel.activities.LoginActivity;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        ComponentName comp = new ComponentName(context.getPackageName(),
                MsgService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

        /*
        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        */
    }

}