package io.goodway.infotel.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.EventAdapter;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.Event;
import io.goodway.infotel.model.User;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.Constants;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class EventsActivity extends AppCompatActivity implements Callback<Event>, SwipeRefreshLayout.OnRefreshListener {

    // GCM SERVICE
    // Allow communication with server to display notifications to device
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;
    private EventAdapter adapter;
    private User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        JodaTimeAndroid.init(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.events);


        recyclerView = (RecyclerView) findViewById(R.id.events);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout.setOnRefreshListener(this);

        layoutManager = new LinearLayoutManager(this);
        adapter = new EventAdapter(this, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume(){
        super.onResume();
        refresh();
    }

    private void refresh(){
        adapter.clear();
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
                            DateTime date_start=null;
                            DateTime date_end=null;
                            if(!event.optString("date_start").equals("null")){date_start = ISODateTimeFormat.dateTime().parseDateTime(event.optString("date_start"));}
                            if(!event.optString("date_end").equals("null")){date_end = ISODateTimeFormat.dateTime().parseDateTime(event.optString("date_end"));}
                            final Event e = new Event(
                                    event.optInt("id"),
                                    event.optString("name"),
                                    date_start,
                                    date_end,
                                    event.optString("avatar"),
                                    event.optInt("type"),
                                    event.optDouble("place_lat_start"),
                                    event.optDouble("place_lng_start"),
                                    event.optDouble("place_lat_end"),
                                    event.optDouble("place_lng_end"),
                                    event.optInt("creator_id"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(e);
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
    public void callback(Event event) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constants.EVENT, event);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onRefresh() {
        refresh();
        swipeRefreshLayout.setRefreshing(false);
    }
}
