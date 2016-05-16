package io.goodway.infotel.sync;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import io.goodway.infotel.model.Event;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.model.communication.Subscription;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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

    public static void event(final Callback callback, String id){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/events/"+id)
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

    public static void inscriptions(final Callback callback, Event e){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/events/"+e.getId()+"/inscriptions")
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void registerToEvent(final Callback callback, User activeUser, Event e){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(activeUser.getId()))
                .add("event_id", String.valueOf(e.getId()))
                .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/inscriptions/add")
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void unRegisterToEvent(final Callback callback, User activeUser, Event e){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("event_id", String.valueOf(e.getId()))
                .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/users/"+activeUser.getId()+"/inscriptions/")
                .delete(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void inscriptions(final Callback callback, User user){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/users/"+user.getId()+"/inscriptions")
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void createEvent(final Callback callback, User activeUser, Event event){
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder b = new FormBody.Builder();
        b.add("name", event.getName());
        b.add("type", String.valueOf(event.getType()));
        if(event.getAvatar()!=null){b.add("avatar", event.getAvatar());}
        b.add("date_start", event.getDate_start().toDateTimeISO().toString());
        Log.d(TAG, event.getDate_start().toString());
        if(event.getDate_end()!=null){b.add("date_end", event.getDate_end().toString());}
        b.add("place_lat_start", String.valueOf(event.getPlace_lat_start()));
        b.add("place_lon_start", String.valueOf(event.getPlace_lon_start()));

        if(event.getPlace_lat_end()!=0){b.add("place_lat_end", String.valueOf(event.getPlace_lat_end()));}
        if(event.getPlace_lon_end()!=0){b.add("place_lon_end", String.valueOf(event.getPlace_lon_end()));}

        Log.d(TAG, String.valueOf(activeUser.getId()));
        b.add("creator_id", String.valueOf(activeUser.getId()));
        RequestBody formBody = b.build();

        Log.d(TAG, "creator_id="+String.valueOf(activeUser.getId()));

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/events/add")
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

    public static void upload(final Callback callback, File f, MediaType type){
        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("file", f.getName())
                .addFormDataPart("file", f.getName(), RequestBody.create(type, f))
                .build();

        Request request = new Request.Builder()
                .url("http://infotel.goodway.io/api/uploads")
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }


    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    public interface Action<T>{
        public void action(T t);
    }

    public interface FinishAction {
        public void action(int length);
    }

}
