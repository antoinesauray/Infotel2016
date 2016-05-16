package io.goodway.infotel.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.UserAdapter;
import io.goodway.infotel.model.Event;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.Image;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Detailed profile
 * @author Antoine Sauray
 * @version 2.0
 */
public class EventActivity extends AppCompatActivity implements OnMapReadyCallback, io.goodway.infotel.callbacks.Callback<Channel> {

    // ----------------------------------- Model
    /**
     * Unique identifier for this activity
     */
    private static final String TAG = "STOP_ACTIVITY";

    // ----------------------------------- UI

    /**
     * Toolbar widget
     */
    private Toolbar toolbar;
    private Message message;
    private Event event;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private UserAdapter adapter;

    private TextView name, inscriptions, inscription;
    private ImageView icon;

    private User activeUser;

    private int request;
    public static final int NEWACTIVITY=3;

    private boolean registered=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Bundle extras = this.getIntent().getExtras();
        message = extras.getParcelable(Constants.MESSAGE);
        activeUser = extras.getParcelable(Constants.USER);
        final ImageView avatar = (ImageView) findViewById(R.id.avatar);

        recyclerView = (RecyclerView) findViewById(R.id.users);
        recyclerView.setHasFixedSize(true);

        inscriptions = (TextView) findViewById(R.id.inscriptions);
        name = (TextView) findViewById(R.id.name);
        icon = (ImageView) findViewById(R.id.icon);
        inscription = (TextView) findViewById(R.id.inscription);


        layoutManager = new LinearLayoutManager(this);
        adapter = new UserAdapter(this, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        HttpRequest.event(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONObject obj = jsonResult.optJSONObject("event");
                        int length = obj.length();
                            DateTime date_start=null;
                            DateTime date_end=null;

                        if(!obj.optString("date_start").equals("null")){date_start = ISODateTimeFormat.dateTime().parseDateTime(obj.optString("date_start"));}
                        if(!obj.optString("date_end").equals("null")){date_end = ISODateTimeFormat.dateTime().parseDateTime(obj.optString("date_end"));}
                            final Event e = new Event(
                                    obj.optInt("id"),
                                    obj.optString("name"),
                                    date_start,
                                    date_end,
                                    obj.optString("avatar"),
                                    obj.optInt("type"),
                                    obj.optDouble("place_lat_start"),
                                    obj.optDouble("place_lng_start"),
                                    obj.optDouble("place_lat_end"),
                                    obj.optDouble("place_lng_end"),
                                    obj.optInt("creator_id"));
                            EventActivity.this.event = e;
                            //checkInscription(e);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if(e.getAvatar()!=null && !e.getAvatar().isEmpty()) {
                                        name.setText(e.getName());
                                        switch (e.getType()){
                                            case Event.CARPOOLING:
                                                icon.setImageResource(R.mipmap.ic_directions_car_white_24dp);
                                                break;
                                            case Event.RUNNING:
                                                icon.setImageResource(R.mipmap.ic_directions_run_white_24dp);
                                                break;
                                            case Event.AFTERWORK:
                                                icon.setImageResource(R.mipmap.ic_local_cafe_white_24dp);
                                                break;
                                            default:
                                                icon.setImageResource(R.mipmap.ic_event_white_24dp);
                                                break;
                                        }
                                        Picasso.with(EventActivity.this)
                                                .load(e.getAvatar())
                                                //.error(R.mipmap)
                                                .fit().centerCrop()
                                                .transform(new Image.ImageTransCircleTransform())
                                                .into(avatar);
                                    }
                                    MapFragment mapFragment = (MapFragment) getFragmentManager()
                                            .findFragmentById(R.id.map);
                                    mapFragment.getMapAsync(EventActivity.this);
                                }
                            });
                        HttpRequest.inscriptions(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.code() == 200) {
                                    JSONObject jsonResult = null;
                                    try {
                                        jsonResult = new JSONObject(response.body().string());
                                        JSONArray events = jsonResult.optJSONArray("inscriptions");
                                        final int length = events.length();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                inscriptions.setText(length+" inscris");
                                            }
                                        });
                                        for(int i=0;i<length;i++){
                                            final JSONObject inscription = events.optJSONObject(i);
                                            HttpRequest.user(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {

                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    if(response.code()==200){
                                                        try {
                                                            JSONObject jsonResult = new JSONObject(response.body().string());
                                                            JSONObject user = jsonResult.optJSONObject("user");
                                                            final User u = new User(
                                                                    user.optInt("id"),
                                                                    user.optString("firstname"),
                                                                    user.optString("lastname"),
                                                                    user.optString("mail"),
                                                                    user.optString("avatar"),
                                                                    user.optString("token"));
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                   adapter.add(u);
                                                                }
                                                            });

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                }
                                            }, inscription.optInt("user_id"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, event);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }, message.getAttachment());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.event);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng center = new LatLng(event.getPlace_lat_start(), event.getPlace_lon_start());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12));
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .title(event.getName())
                .snippet(getString(R.string.start_point))
                .position(center));
        marker.showInfoWindow();
    }

    @Override
    public void callback(Channel channel) {

    }

    public void inscription(View v){
        inscriptionStateChange();
    }

    private void inscriptionStateChange(){
        Toast.makeText(this, "Fonctionnalité indisponible", Toast.LENGTH_SHORT).show();
        /*
        registered = !registered;
        if(registered){
            HttpRequest.registerToEvent(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if(response.code()==200){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code() == 200) {
                                    inscription.setTextColor(getResources().getColor(R.color.colorAccent));
                                    inscription.setText(R.string.suscribed);
                                } else {
                                    Toast.makeText(EventActivity.this, "Echec", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
            }, activeUser, event);
        }
        else{
            HttpRequest.unRegisterToEvent(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == 200) {
                                inscription.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                                inscription.setText(R.string.not_suscribed);
                            } else {
                                Toast.makeText(EventActivity.this, "Echec", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }, activeUser, event);
            }
            */
    }
    private void checkInscription(final Event e){
        HttpRequest.inscriptions(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray events = jsonResult.optJSONArray("inscriptions");
                        final int length = events.length();
                        for (int i = 0; i < length; i++) {
                            final JSONObject inscription = events.optJSONObject(i);
                            if(inscription.optInt("event_id")==e.getId()){
                                registered = true;
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                inscriptionStateChange();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }}, activeUser);
    }
}
