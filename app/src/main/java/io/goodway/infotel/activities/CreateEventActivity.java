package io.goodway.infotel.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.SendingChannelAdapter;
import io.goodway.infotel.model.Event;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.File;
import io.goodway.infotel.utils.Preferences;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class CreateEventActivity extends AppCompatActivity{

    // GCM SERVICE
    // Allow communication with server to display notifications to device
    private static final String TAG = "CreateEventActivity";
    private Place place;

    private EditText name;
    private RadioGroup radioGroup;
    private DatePicker datePicker;
    private String avatar;

    private User activeUser;

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        activeUser = getIntent().getExtras().getParcelable(Constants.USER);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        name = (EditText) findViewById(R.id.name);
        radioGroup = (RadioGroup) findViewById(R.id.radio);
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        //setLocation = (Button) findViewById(R.id.setLocation);
        //location = (TextView) findViewById(R.id.location);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                this.place = place;
            }
        }
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

    public void placePicker(View v){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), Constants.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void fabClick(View v){
        String eventName = name.getText().toString();
        if(eventName.isEmpty()){
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
            return;
        }
        int idRadio = radioGroup.getCheckedRadioButtonId();
        int type=-1;
        switch (idRadio){
            case R.id.carpooling:
                type = Event.CARPOOLING;
                break;
            case R.id.running:
                type = Event.RUNNING;
                break;
            case R.id.afterwork:
                type = Event.AFTERWORK;
                break;
            case R.id.infotel_event:
                type = Event.INFOTEL;
                break;
        }
        if(type==-1){
            Toast.makeText(this, R.string.enter_type, Toast.LENGTH_SHORT).show();
            return;
        }

        double place_lat_start=0, place_lon_start=0, place_lat_end=0, place_lon_end=0;
        if(place!=null){
            LatLng latLng =  place.getLatLng();
            place_lat_start = latLng.latitude;
            place_lon_start = latLng.longitude;
        }
            final Event e = new Event(-1, eventName, new DateTime(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0), null,
                avatar, type, place_lat_start, place_lon_start, 0, 0, activeUser.getId());
        HttpRequest.createEvent(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                finish();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    try {
                        JSONObject jsonResult = new JSONObject(response.body().string()).optJSONObject("event");
                        e.setId(jsonResult.optInt("id"));
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(Constants.EVENT, e);
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                } catch (JSONException e1) {
                        e1.printStackTrace();
                        finish();
                    }
                }
                else{
                    Log.d(TAG, "code="+response.code());
                    finish();
                }
        }}, activeUser, e);
    }
}
