package io.goodway.infotel.sync;


import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.model.communication.Subscription;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by antoine on 5/14/16.
 */
public class HttpRequest {

    private static String TAG = "HttpRequest";

    public static void messageToTopic(final Callback callback, Channel channel, Message message){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(message.getSender_id()))
                .add("avatar", message.getAvatar())
                .add("channel_id", String.valueOf(channel.getId()))
                .add("content", message.getContent())
                .add("attachment_type", String.valueOf(message.getAttachment_type()))
                .add("attachment", message.getAttachment())
                .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/messages/add")
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void auth(final Callback callback, String mail, String password){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("mail", mail)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/auth")
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void subscriptions(final Callback callback, User activeUser){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/users/"+activeUser.getId()+"/channels")
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void channel(final Callback callback, Subscription subscription){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/channels/"+subscription.getChannel_id())
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void channels(final Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/channels/all")
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void channels(final Callback callback, User activeUser){
        HttpRequest.subscriptions(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray subscriptions = jsonResult.optJSONArray("subscriptions");
                        int length=subscriptions.length();
                        if(length==0){

                        }
                        else{
                            for(int i=0;i<length;i++){
                                JSONObject obj = subscriptions.getJSONObject(i);
                                HttpRequest.channel(callback, new Subscription(obj.optInt("id"), obj.optInt("channel_id"), obj.optInt("user_id")));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.d(TAG, "http: error "+response.code());
                }
            }
        }, activeUser);
    }

    public static void addSubscription(final Callback callback, User activeUser, Channel channel){
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("channel_id", String.valueOf(channel.getId()))
                .add("user_id", String.valueOf(activeUser.getId()))
                .add("","")
                .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/subscriptions/add")
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void messages(final Callback callback, Channel channel){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/channels/"+channel.getId()+"/messages")
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void user(final Callback callback, int id){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/users/"+id)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

}
