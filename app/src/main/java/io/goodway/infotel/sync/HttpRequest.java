package io.goodway.infotel.sync;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.goodway.infotel.model.Event;
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

    public static void events(final Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/events/all")
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void createEvent(final Callback callback, User activeUser, Event event){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("name", event.getName())
                .add("avatar", event.getAvatar())
                .add("date_start", event.getDate_start().toString())
                .add("date_end", event.getDate_end().toString())
                .add("place_lat_start", String.valueOf(event.getPlace_lat_start()))
                .add("place_lng_start", String.valueOf(event.getPlace_lon_start()))
                .add("place_lat_end", String.valueOf(event.getPlace_lat_end()))
                .add("place_lng_end", String.valueOf(event.getPlace_lon_end()))
                .add("creator_id", String.valueOf(activeUser.getId()))
        .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/auth")
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void channels(final Action<Channel> action, final FinishAction finishAction, User activeUser){

        new AsyncTask<User, Channel, Integer>() {
            @Override
            protected Integer doInBackground(User... params) {
                OkHttpClient client = new OkHttpClient();
                int length=0;
                Request request = new Request.Builder()
                        .url("http://infotel.goodway.io/api/users/"+params[0].getId()+"/channels")
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if(response.code()==200){
                        JSONObject jsonResult = null;
                        try {
                            jsonResult = new JSONObject(response.body().string());
                            JSONArray subscriptions = jsonResult.optJSONArray("subscriptions");
                            length=subscriptions.length();
                            for(int i=0;i<length;i++){
                                JSONObject obj = subscriptions.getJSONObject(i);
                                Request request2 = new Request.Builder().url("http://infotel.goodway.io/api/channels/"+obj.optInt("channel_id")).build();
                                Call call2 = client.newCall(request2);
                                Response response2 = call2.execute();
                                JSONObject obj2 = new JSONObject(response2.body().string());
                                Log.d(TAG, obj2.toString());
                                JSONObject channel = obj2.getJSONObject("channel");
                                publishProgress(new Channel(channel.optInt("id"), channel.optString("name"), channel.optString("full_name"), channel.optString("avatar")));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Log.d(TAG, "http: error "+response.code());
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return length;
            }
            @Override
            protected void onProgressUpdate(Channel...progress){
                action.action(progress[0]);
            }
            protected void onPostExecute(Integer length){
                finishAction.action(length);
            }
        }.execute(activeUser);
    }

    public static void channelsOnThread(final Callback callback, User activeUser){
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

    public interface Action<T>{
        public void action(T t);
    }

    public interface FinishAction {
        public void action(int length);
    }

}
