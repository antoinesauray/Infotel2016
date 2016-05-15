/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.goodway.infotel.sync.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import io.goodway.infotel.R;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.model.communication.Notification;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.Image;

public class GCMService extends GcmListenerService {

    private static final String TAG = "GCMService";
    public static final String MESSAGE_RECEIVED="MessageReceived";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        Log.d(TAG, data.toString());
        Log.d(TAG, "From: " + from);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
            int sender_id = Integer.parseInt(data.getString("sender_id"));
            int channel_id = Integer.parseInt(data.getString("channel_id"));
            String name = data.getString("name");
            String avatar = data.getString("avatar");
            String content = data.getString("content");
            int attachment_type = Integer.parseInt(data.getString("attachment_type"));
            String attachment_url = data.getString("attachment");

            String date = data.getString("date");


            Log.d(TAG, "ype="+attachment_type);

            Message m = new Message(sender_id, name, avatar, content, attachment_type, attachment_url, false);

            // Notify UI that registration has completed, so the progress indicator can be hidden.
            Log.d("sender", "Broadcasting message");
            Intent intent = new Intent(GCMService.MESSAGE_RECEIVED);
            intent.putExtra(Constants.MESSAGE, m);
            intent.putExtra(Constants.CHANNEL, channel_id);
            // You can also include some extra data.
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);;
        } else {
            Bundle notification = data.getBundle("notification");
            String title = notification.getString("title");
            String body = notification.getString("body");
            // normal downstream message.
            if(data.getString("type").equals("notification")){
                String large_icon = data.getString("icon");
                sendNotification(new Notification(title, large_icon, body));
            }
            else if(data.getString("type").equals("message")){
                int sender_id = data.getInt("sender_id");
                String name = data.getString("name");
                String avatar = data.getString("avatar");
                String content = data.getString("content");
                int attachment_type = data.getInt("attachment_type");
                String attachment_url = data.getString("attachment");

                String date = data.getString("date");

                Message m = new Message(sender_id, name, avatar, content, attachment_type, attachment_url, false);

                // Notify UI that registration has completed, so the progress indicator can be hidden.
                Log.d("sender", "Broadcasting message");
                Intent intent = new Intent(GCMService.MESSAGE_RECEIVED);
                intent.putExtra("message", m);
                // You can also include some extra data.
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);;
            }
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */

        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param n Notification object.
     */
    private void sendNotification(Notification n) {
        Intent intent = new Intent(this, MsgService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(n.getTitle())
                .setLargeIcon(Image.GetBitmapClippedCircle(Image.getBitmapFromURL(n.getIcon())))
                .setContentText(n.getContent())
                .setAutoCancel(true)
                //.setColor(getColor(R.color.colorAccent))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    /*

        Bundle args = new Bundle();
        args.putString("mobno", mobno);
        args.putString("name", name);
        args.putString("msg", msg);
        Intent chat = new Intent(this, ChatActivity.class);
        chat.putExtra("INFO", args);
        notification = new NotificationCompat.Builder(this);
        notification.setContentTitle(name);
        notification.setContentText(msg);
        notification.setTicker("New Message !");
        notification.setSmallIcon(R.drawable.ic_launcher);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000,
                chat, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setContentIntent(contentIntent);
        notification.setAutoCancel(true);
        manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
     */
}