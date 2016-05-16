package io.goodway.infotel.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.DiscoveredChannelAdapter;
import io.goodway.infotel.adapters.EventAdapter;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.Event;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.Constants;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class EventsActivity extends AppCompatActivity implements Callback<Channel> {

    // GCM SERVICE
    // Allow communication with server to display notifications to device
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private EventAdapter adapter;
    private User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);


        recyclerView = (RecyclerView) findViewById(R.id.events);


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        adapter = new EventAdapter(this, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        HttpRequest.events(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray events = jsonResult.optJSONArray("events");
                        int length = events.length();
                        for(int i=0;i<length;i++){
                            final JSONObject event = events.optJSONObject(i);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        adapter.add(new Event(
                                                event.optString("name"),
                                                new SimpleDateFormat("dd/mm/yyyy").parse(event.optString("date_start")),
                                                new SimpleDateFormat("dd/mm/yyyy").parse(event.optString("date_end")),
                                                event.optString("avatar"),
                                                event.optInt("type"),
                                                event.optDouble("place_lat_start"),
                                                event.optDouble("place_lng_start"),
                                                event.optDouble("place_lat_end"),
                                                event.optDouble("place_lng_end"),
                                                event.optInt("creator_id")));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }});
    }

    @Override
    public void callback(Channel channel) {

    }
}
