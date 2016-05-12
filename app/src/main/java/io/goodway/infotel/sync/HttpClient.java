package io.goodway.infotel.sync;

/**
 * Created by antoine on 5/11/16.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Message;


public class HttpClient{


public static class Get<T> extends AsyncTask<AbstractMap.SimpleEntry<String, String>, T, Integer> {

    private Context c;
    private Action<T> action;
    private ErrorAction error;
    private FinishAction finish;
    private ProcessJson<T> processJson;
    private String url;

    private Get(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, final String URL){
        this.c = c;
        this.action = action;
        this.error = error;
        this.processJson = processJson;
        this.url = URL;
    }

    public static AsyncTask getUberEstimate(Context c, Action<List<User>> action, ErrorAction error, double start_latitude, double start_longitude, double end_latitude, double end_longitude) {
        return new Get<>(c, new ProcessJson<List<User>>() {
            @Override
            public List<User> processJson(JSONObject jsonObject) throws JSONException {
                ArrayList<User> ret = new ArrayList<User>();
                JSONArray jsonArray = jsonObject.getJSONArray("prices");
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject innerJsonObject = jsonArray.getJSONObject(i);
                    /*
                    String localized_display_name = innerJsonObject.optString("localized_display_name");
                    int high_estimate = innerJsonObject.optInt("high_estimate");
                    */
                    ret.add(new User());
                }
                return ret;
            }
        }, action, error, "https://developer.goodway.io/api/v1/uber/estimate?").execute(
                new AbstractMap.SimpleEntry<String, String>("start_latitude", Double.toString(start_latitude)),
                new AbstractMap.SimpleEntry<String, String>("start_longitude", Double.toString(start_longitude)),
                new AbstractMap.SimpleEntry<String, String>("end_latitude", Double.toString(end_latitude)),
                new AbstractMap.SimpleEntry<String, String>("end_longitude", Double.toString(end_longitude)));
    }


    @Override
    protected Integer doInBackground(AbstractMap.SimpleEntry<String, String>... entries) {
        int length=0;
        try {
            HttpsURLConnection urlConnection = HttpProtocol.getHttpsGetUrlConnection(this.url, entries);
            Log.d("url=", "url= "+urlConnection.toString());
            int serverResponseCode = urlConnection.getResponseCode();
            String serverResponseMessage = urlConnection.getResponseMessage();

            Log.d(serverResponseCode+"", "Response code");
            Log.d(serverResponseMessage, "Response message");
            String jsonResult;
            if (serverResponseCode == 201 || serverResponseCode == 200) {
                Log.d(urlConnection.getResponseCode() + "", "response code");
                InputStream response = urlConnection.getInputStream();
                jsonResult = HttpProtocol.convertStreamToString(response);
                Log.d("response:", jsonResult.toString());
                try {
                    JSONObject obj= new JSONObject(jsonResult.toString());
                    publishProgress(processJson.processJson(obj));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Log.d("error", "json exception");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            length=-1;
            e.printStackTrace();
        }
        return length;
    }
    @Override
    protected void onProgressUpdate(T...progress){
        action.action(progress[0]);
    }

    protected void onPostExecute(Integer length){
        if(length<1 && error!=null){
            error.action(length);
        }
        else if(finish!=null){
            finish.action(length);
        }
    }
}

    public static class Post<T> extends AsyncTask<AbstractMap.SimpleEntry<String, String>, T, Integer> {

        private Context c;
        private Action<T> action;
        private ErrorAction error;
        private FinishAction finish;
        private ProcessJson<T> processJson;
        private String url;

        private Post(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, final String URL) {
            this.c = c;
            this.action = action;
            this.error = error;
            this.processJson = processJson;
            this.url = URL;
        }

        private Post(Context c, ProcessJson<T> processJson, Action<T> action, ErrorAction error, FinishAction finish, final String URL) {
            this.c = c;
            this.action = action;
            this.error = error;
            this.finish = finish;
            this.processJson = processJson;
            this.url = URL;
        }

        public static AsyncTask messageInTopic(final Context c, Action<Boolean> action, Message message, String topic){
            return new Post<>(c, new ProcessJson<Boolean>() {
                @Override
                public Boolean processJson(JSONObject jsonObject) throws JSONException {
                    return jsonObject.optBoolean("success");
                }
            }, action, null, null, "http://infotel.goodway.io/api/v1/message/post").execute(
                    new AbstractMap.SimpleEntry<String, String>("type", message.getAttachment_type()+""),
                    new AbstractMap.SimpleEntry<String, String>("content", message.getContent()+""),
                    new AbstractMap.SimpleEntry<String, String>("sender_id", "1"),
                    new AbstractMap.SimpleEntry<String, String>("topic", topic));
        }

        @Override
        protected Integer doInBackground(AbstractMap.SimpleEntry<String, String>... entries) {

            int length = 0;
            try {
                HttpURLConnection urlConnection = HttpProtocol.getHttpPostUrlConnection(this.url, entries);
                Log.d("url=", "url= " + urlConnection.toString());
                int serverResponseCode = urlConnection.getResponseCode();
                String serverResponseMessage = urlConnection.getResponseMessage();
                Log.d(serverResponseCode + "", "Response code");
                Log.d(serverResponseMessage, "Response message");
                String jsonResult;
                if (serverResponseCode == 201 || serverResponseCode == 200) {
                    Log.d(urlConnection.getResponseCode() + "", "response code");
                    InputStream response = urlConnection.getInputStream();
                    jsonResult = HttpProtocol.convertStreamToString(response);
                    Log.d("response:", jsonResult.toString());
                    try {
                        JSONObject obj = new JSONObject(jsonResult.toString());
                        publishProgress(processJson.processJson(obj));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Log.d("error", "json exception");
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                length = -1;
                e.printStackTrace();
            }
            return length;
        }

        @Override
        protected void onProgressUpdate(T... progress) {
            action.action(progress[0]);
        }

        protected void onPostExecute(Integer length) {
            if (length < 1 && error != null) {
                error.action(length);
            } else if (finish != null) {
                finish.action(length);
            }
        }

    }
    /**
     * @author Alexis Robin
     * @version 0.6
     * Licensed under the Apache2 license
     */
    public interface Action<T> {
        public void action(T e);
    }

    public interface ErrorAction {
        public void action(int length);
    }

    public interface FinishAction {
        public void action(int ret);
    }

    public interface ProcessJson<T> {
        public T processJson(JSONObject jsonObject) throws JSONException;
    }


}