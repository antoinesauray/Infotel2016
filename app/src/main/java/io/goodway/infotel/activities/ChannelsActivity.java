package io.goodway.infotel.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.ChannelAdapter;
import io.goodway.infotel.adapters.DiscoveredChannelAdapter;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.fragments.ChatFragment;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Subscription;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.sync.gcm.QuickstartPreferences;
import io.goodway.infotel.sync.gcm.RegistrationIntentService;
import io.goodway.infotel.utils.Constants;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class ChannelsActivity extends AppCompatActivity implements Callback<Channel>, SwipeRefreshLayout.OnRefreshListener {

    // GCM SERVICE
    // Allow communication with server to display notifications to device
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DiscoveredChannelAdapter adapter;

    private User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);

        Bundle extras = getIntent().getExtras();
        activeUser = extras.getParcelable(Constants.USER);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.channels);


        recyclerView = (RecyclerView) findViewById(R.id.channels);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        adapter = new DiscoveredChannelAdapter(this, this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.clear();
        HttpRequest.channels(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray channels = jsonResult.optJSONArray("channels");
                        int length=channels.length();
                        if(length==0){
                            // On a un probleme
                        }
                        else{
                            ArrayList<Channel> channel_list = new ArrayList<Channel>();
                            for(int i=0;i<length;i++){
                                JSONObject obj = channels.getJSONObject(i);
                                Log.d(TAG, obj.optString("name"));
                                channel_list.add(new Channel(obj.optInt("id"), obj.optString("name"), obj.optString("full_name"), obj.optString("avatar")));
                            }
                            adapter.add(channel_list);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
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
    public void callback(Channel channel) {
        HttpRequest.addSubscription(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==201){
                    finish();
                    //Intent i = new Intent(ChannelsActivity.this, MainActivity.class);
                    //i.putExtra(Constants.USER, activeUser);
                    //startActivity(i);
                }
                else{
                    Log.d(TAG, "error "+response.code());
                    Toast.makeText(ChannelsActivity.this, "Vous êtes déjà présent dans ce channel", Toast.LENGTH_SHORT).show();
                }
            }
        }, activeUser, channel);
    }

    @Override
    public void onRefresh() {
        onResume();
    }
}
