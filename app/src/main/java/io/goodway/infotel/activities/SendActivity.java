package io.goodway.infotel.activities;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.SendingChannelAdapter;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.File;
import io.goodway.infotel.utils.Preferences;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class SendActivity extends AppCompatActivity implements View.OnClickListener {

    // GCM SERVICE
    // Allow communication with server to display notifications to device
    private static final String TAG = "SendActivity";
    private RecyclerView recyclerView;
    private SendingChannelAdapter adapter;

    private User activeUser;
    private EditText comment;
    private ImageButton send;

    private int attachment_type;
    private String attachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        String email = Preferences.getStringPreference(this, "email");
        String password = Preferences.getStringPreference(this, "password");
        HttpRequest.auth(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){
                    try {
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        JSONObject user = jsonResult.optJSONObject("user");
                        SendActivity.this.activeUser = new User(
                                user.optInt("id"),
                                user.optString("firstname"),
                                user.optString("lastname"),
                                user.optString("mail"),
                                user.optString("avatar"),
                                user.optString("token"));

                        Log.d(TAG, activeUser.toString());

                        HttpRequest.channelsOnThread(new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if(response.code()==200){
                                    JSONObject jsonResult = null;
                                    try {
                                        jsonResult = new JSONObject(response.body().string());
                                        final JSONObject channels = jsonResult.optJSONObject("channel");
                                        Log.d(TAG, "suscribing to "+channels.optString("name"));
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.add(new Channel(channels.optInt("id"), channels.optString("name"), channels.optString("full_name"), channels.optString("avatar")));
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, SendActivity.this.activeUser);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else if(response.code()==401){
                    Toast.makeText(SendActivity.this, R.string.auth_failure, Toast.LENGTH_SHORT).show();
                }
            }
        }, email, password);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("test");

        comment = (EditText) findViewById(R.id.comment);
        send = (ImageButton) findViewById(R.id.send);

        send.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.channels);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        adapter = new SendingChannelAdapter(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                attachment_type = Message.IMAGE;
                handleSendImage(intent); // Handle single image being sent
            }
        }
        else {
            // Handle other intents, such as being started from the home screen
        }
    }

    void handleSendText(Intent intent) {
        String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (url != null) {
            // Update UI to reflect text being shared
            Log.d(TAG, "Received intent with url="+url);
            if (File.isImageFile(url)){
                Log.d(TAG, "image");
                attachment_type = Message.IMAGE;
                attachment=url;
            }
            else if(File.isVideoFile(url)){
                Log.d(TAG, "video");
                attachment_type = Message.VIDEO;
                attachment=url;
            }
            else if(File.isPdfFile(url)){
                Log.d(TAG, "youtube");
                attachment_type = Message.PDF;
                attachment=url;
            }
            else{
                Log.d(TAG, "file");
                attachment_type = Message.FILE;
                attachment=url;
            }
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            attachment = imageUri.getPath();
        }
    }

    void handleYoutube(Intent intent) {
        String id = intent.getExtras().getString(Intent.EXTRA_TEXT);
        if (id != null) {
            // Update UI to reflect image being shared
            attachment = id;
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

    @Override
    public void onClick(View v) {
        if(activeUser!=null) {
            String userComment = comment.getText().toString();
            Collection<Channel> channels = adapter.getSelectedChannels();
            Message m = new Message(activeUser, userComment, attachment_type, attachment, true);
            Log.d(TAG, m.toString());
            Log.d(TAG, "type="+attachment_type);
            Log.d(TAG, "attachment="+attachment);
            for (Channel c : channels) {
                HttpRequest.messageToTopic(new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 201) {
                            //Toast.makeText(ChatFragment.this.getContext(), "Message reçu par le serveur", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, response.code() + "");
                            finish();
                        } else {
                            Log.d(TAG, response.code() + "");
                        }
                    }
                }, c, m);
            }
        }
        else{
            Toast.makeText(SendActivity.this, R.string.not_authenticated, Toast.LENGTH_SHORT).show();
        }
    }
}
