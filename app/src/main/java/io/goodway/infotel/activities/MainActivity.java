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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.fragments.ChatFragment;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.model.communication.Subscription;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.sync.gcm.GCMService;
import io.goodway.infotel.sync.gcm.QuickstartPreferences;
import io.goodway.infotel.sync.gcm.RegistrationIntentService;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.Debug;
import io.goodway.infotel.utils.Image;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class MainActivity extends AppCompatActivity implements Callback<Channel> {

    // GCM SERVICE
    // Allow communication with server to display notifications to device
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    //
    private String[] titles;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private MenuItem currentItemSelected;

    private RecyclerView recyclerView;
    private ChannelAdapter adapter;
    private View noChannels;
    private View connexionFailed;

    private ChatFragment chatFragment;

    private User activeUser;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Message message = intent.getParcelableExtra("message");
            if(message.getSender_id()!= Debug.SENDER_ID){
                Log.d("displaying type", "type="+message.getAttachment_type());
                Log.d("displaying image", "url="+message.getAttachment());

            }
        }
    };

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        activeUser = extras.getParcelable(Constants.USER);

        setupGCM();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(GCMService.MESSAGE_RECEIVED));

        Log.d(TAG, activeUser.toString());
        ((TextView)findViewById(R.id.name)).setText(activeUser.getFirstame()+" "+activeUser.getLastname());
        ((TextView)findViewById(R.id.mail)).setText(activeUser.getMail());

        if(activeUser.getAvatar()!=null && !activeUser.getAvatar().isEmpty()){
            Picasso.with(this)
                    .load(activeUser.getAvatar())
                    .error(R.mipmap.ic_person_white_36dp)
                    .fit().centerInside()
                    .transform(new Image.ImageTransCircleTransform())
                    .into((ImageView) findViewById(R.id.avatar));
        }

        if(getIntent().getExtras()!=null) {
            Log.d(TAG, getIntent().getExtras().toString());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noChannels = findViewById(R.id.no_channels);
        connexionFailed = findViewById(R.id.connexion_failed);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        titles = new String[]{"Accueil",
                //"Notifications",
                "Profil"};
        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.mipmap.ic_launcher);
        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_public_white_48dp);
        //tabLayout.getTabAt(1).setIcon(R.mipmap.ic_launcher);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this,this.drawerLayout,0,0);
        //navigationView = (NavigationView) findViewById(R.idnav);

        drawerLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("NICK", "button button button..................");
            }
        });

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    drawerLayout.closeDrawers();  // CLOSE DRAWER
                    return true;
                }
            });
        }
        recyclerView = (RecyclerView) findViewById(R.id.channels);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        adapter = new ChannelAdapter(this, this);


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        drawerToggle.syncState();
        adapter.clear();
        HttpRequest.subscriptions(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                connexionFailed.setVisibility(View.VISIBLE);
                noChannels.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connexionFailed.setVisibility(View.GONE);
                    }
                });
                if(response.code()==200){
                    JSONObject jsonResult = null;
                    try {
                        jsonResult = new JSONObject(response.body().string());
                        JSONArray subscriptions = jsonResult.optJSONArray("subscriptions");
                        int length=subscriptions.length();
                        if(length==0){
                            Log.d(TAG, "No subscriptions");

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    noChannels.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        else{
                            Log.d(TAG, "Found "+length+" subscriptions");
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    noChannels.setVisibility(View.GONE);
                                }
                            });
                            for(int i=0;i<length;i++){
                                JSONObject obj = subscriptions.getJSONObject(i);
                                HttpRequest.channel(new okhttp3.Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                connexionFailed.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        if(response.code()==200) {
                                            JSONObject jsonResult = null;
                                            try {
                                                jsonResult = new JSONObject(response.body().string());
                                                JSONObject channels = jsonResult.optJSONObject("channel");
                                                int id = channels.optInt("id");
                                                String name = channels.optString("name");
                                                String avatar = channels.optString("avatar");
                                                adapter.add(new Channel(id, name, avatar));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else{
                                            Log.d(TAG, "http: error "+response.code());
                                        }
                                    }
                                }, new Subscription(obj.optInt("id"), obj.optInt("channel_id"), obj.optInt("user_id")));
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // synchroniser le drawerToggle après la restauration via onRestoreInstanceState
        drawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        chatFragment = ChatFragment.newInstance(getIntent().getExtras());

        adapter.addFragment(chatFragment);
        //adapter.addFragment(chatFragment);

        viewPager.setAdapter(adapter);
    }

    @Override
    public void callback(Channel channel) {
        drawerLayout.closeDrawers();  // CLOSE DRAWER
        chatFragment.switchChannel(channel);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }


    /**
     * Setup the GCM service to listen for incoming notifications
     */
    private void setupGCM(){
        Log.d(TAG, "setupGCM");
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(TAG, "sentToken");
                } else {
                    Log.d(TAG, "Token error");
                }
            }
        };
        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.putExtra(Constants.USER, activeUser);
            startService(intent);
        }
    }

    /**
     * Register the receiver for incoming GCM notifications
     */
    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void add(View v){
        Intent i = new Intent(this, ChannelsActivity.class);
        i.putExtra(Constants.USER, activeUser);
        startActivity(i);
    }
}
