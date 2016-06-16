package io.goodway.infotel.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.goodway.infotel.R;
import io.goodway.infotel.activities.ChannelsActivity;
import io.goodway.infotel.activities.CreateEventActivity;
import io.goodway.infotel.activities.EventsActivity;
import io.goodway.infotel.adapters.MessageAdapter;
import io.goodway.infotel.model.Event;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.sync.gcm.GCMService;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.Image;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class ChatFragment extends Fragment implements TextWatcher, View.OnClickListener, View.OnFocusChangeListener {

    private View root;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ImageButton send, attach;
    private AutoCompleteTextView message;
    private BottomSheetLayout bottomSheet;

    private MessageAdapter adapter;

    private String name="Antoine";
    private String avatar="http://data.goodway.io/img/alexis.jpg";

    private String TAG = "ChatFragment";

    private Channel activeChannel;
    private User activeUser;

    private View selectChannel, connexionFailed;
    private Activity mActivity;

    private File imageAttachment;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Message message = intent.getParcelableExtra(Constants.MESSAGE);
            int channel_id = intent.getIntExtra(Constants.CHANNEL, -1);

            Log.d(TAG, "received message: "+message.toString());
            Log.d(TAG, "channel_id="+channel_id);

            if(activeChannel!=null) {
                Log.d(TAG, "active_channel_id=" + activeChannel.getId());
                if (channel_id > -1 && channel_id == activeChannel.getId() && message.getSender_id() != activeUser.getId()) {
                    Log.d("displaying type", "type=" + message.getAttachment_type());
                    Log.d("displaying image", "url=" + message.getAttachment());
                    adapter.add(message);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public static ChatFragment newInstance(Bundle args) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_chat, container, false);
        JodaTimeAndroid.init(getContext());

        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        send = (ImageButton) root.findViewById(R.id.send);
        message = (AutoCompleteTextView) root.findViewById(R.id.message);
        attach = (ImageButton) root.findViewById(R.id.attach);

        bottomSheet = (BottomSheetLayout) root.findViewById(R.id.bottomsheet);

        message.setOnFocusChangeListener(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        adapter = new MessageAdapter(mActivity, activeUser, recyclerView, layoutManager, null);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        message.addTextChangedListener(this);

        attach.setOnClickListener(this);
        send.setOnClickListener(this);

        selectChannel = root.findViewById(R.id.select_channel);
        connexionFailed = root.findViewById(R.id.connexion_failed);

        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mMessageReceiver,
                new IntentFilter(GCMService.MESSAGE_RECEIVED));

        if(savedInstanceState!=null){
            this.activeChannel = savedInstanceState.getParcelable(Constants.CHANNEL);
            this.activeUser = savedInstanceState.getParcelable(Constants.USER);
            switchChannel(activeChannel);
        }
        else{
            activeUser = getArguments().getParcelable(Constants.USER);
            message.setEnabled(false);
            send.setEnabled(false);
            attach.setEnabled(false);
            message.setAlpha(0.2f);
            send.setAlpha(0.2f);
            attach.setAlpha(0.2f);
        }

        //activeChannel = new Channel(1, "Général");
        return root;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.CHANNEL, activeChannel);
        outState.putParcelable(Constants.USER, activeUser);
    }

    public void onResume(){
        super.onResume();
        bottomSheet.dismissSheet();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(s.length()>0 && !send.isEnabled()){
            send.setEnabled(true);
            send.setAlpha(1f);
        }
        else if(s.length()==0 && send.isEnabled()){
            send.setEnabled(false);
            send.setAlpha(0.2f);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.send:
                String content = message.getText().toString();
                if(content.length()>0) {
                    final Message m = new Message(activeUser.getId(), name, avatar, content, Message.TEXT, "http://www.infotel.com/wp-content/uploads/2013/03/logo.png", true);
                    sendMessage(m);
                }
                break;
            case R.id.attach:
                MenuSheetView menuSheetView =
                        new MenuSheetView(getActivity(), MenuSheetView.MenuType.GRID, R.string.context_post, new MenuSheetView.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch(item.getItemId()){
                                    case R.id.search:
                                        searchEvent();
                                        break;
                                    case R.id.create_event:
                                        createEvent();
                                        break;
                                    case R.id.attach_image:
                                        attachImage();
                                        break;
                                    case R.id.take_picture:
                                        takePicture();
                                        break;
                                    case R.id.take_video:
                                        takeVideo();
                                        break;
                                }
                                if (bottomSheet.isSheetShowing()) {
                                    bottomSheet.dismissSheet();
                                }
                                return true;
                            }
                        });
                menuSheetView.inflateMenu(R.menu.context_menu);
                bottomSheet.showWithSheetView(menuSheetView);
                break;
        }

    }

    public void sendMessage(final Message m){
            adapter.add(m);
            message.getText().clear();
            HttpRequest.messageToTopic(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.code()==201){
                        //Toast.makeText(ChatFragment.this.getContext(), "Message reçu par le serveur", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, response.code()+"");
                    }
                    else{
                        Log.d(TAG, response.code()+"");
                    }
                }
            }, activeChannel, m);
    }


    public void switchChannel(final Channel channel){
        if(channel!=null) {
            this.activeChannel = channel;
            ((AppCompatActivity)mActivity).getSupportActionBar().setTitle("#"+activeChannel.getFullName());
            Log.d(TAG, "switching to channel " + channel.getName());
            if(selectChannel!=null){
                selectChannel.setVisibility(View.GONE);
            }
            if(connexionFailed!=null){connexionFailed.setVisibility(View.GONE);}
            adapter.clear();
            HttpRequest.messages(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connexionFailed.setVisibility(View.VISIBLE);
                            message.setEnabled(false);
                            send.setEnabled(false);
                            attach.setEnabled(false);
                            message.setAlpha(0.2f);
                            send.setAlpha(0.2f);
                            attach.setAlpha(0.2f);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        JSONObject jsonResult = null;
                        try {
                            jsonResult = new JSONObject(response.body().string());
                            JSONArray channels = jsonResult.optJSONArray("messages");
                            int length = channels.length();
                            Log.d(TAG, "retrieved "+length+" messages on channel "+channel.getName());
                            final ArrayList<Message> messages = new ArrayList<Message>();
                            for (int i = 0; i < length; i++) {
                                final JSONObject obj = channels.optJSONObject(i);
                                final int sender_id = obj.optInt("user_id");
                                Log.d(TAG, "sender_id="+sender_id);
                                Log.d(TAG, "active_sender_id="+activeUser.getId());
                                messages.add(new Message(
                                        sender_id,
                                        obj.optString("name"),
                                        obj.optString("avatar"),
                                        obj.optString("content"),
                                        obj.optInt("attachment_type"),
                                        obj.optString("attachment"),
                                        sender_id == activeUser.getId(),
                                        new DateTime(obj.optString("createdAt"))
                                ));
                                adapter.setMessages(messages);

                            }
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerView.scrollToPosition(messages.size());
                                    attach.setEnabled(true);
                                    message.setEnabled(true);
                                    message.setAlpha(1f);
                                    attach.setAlpha(1f);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, channel);
        }
        else{
            Log.d(TAG, "Channel null");
            attach.setEnabled(false);
            send.setEnabled(false);
            message.setEnabled(false);
            message.setAlpha(0.2f);
            send.setAlpha(0.2f);
            attach.setAlpha(0.2f);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d(TAG, "scroll");
        recyclerView.scrollToPosition(adapter.getItemCount());
    }

    public void postEventToCurrentChannel(Event e){
        Log.d(TAG, "posting event message");
        sendMessage(new Message(activeUser, "Linked Event", Message.EVENT, String.valueOf(e.getId()), true));
    }

    private void searchEvent(){
        Intent i = new Intent(mActivity, EventsActivity.class);
        i.putExtra(Constants.USER, activeUser);
        i.putExtra(Constants.CHANNEL, activeChannel);
        startActivityForResult(i, Constants.SEARCH_EVENT_REQUEST);

    }

    private void createEvent(){
        Intent i = new Intent(mActivity, CreateEventActivity.class);
        i.putExtra(Constants.USER, activeUser);
        i.putExtra(Constants.CHANNEL, activeChannel);
        startActivityForResult(i, Constants.CREATE_EVENT_REQUEST);
    }

    private void attachImage(){
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent , Constants.LOAD_IMAGE_REQUEST );
    }

    private void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            imageAttachment=null;
            try {
                imageAttachment = Image.createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imageAttachment != null) {
                Log.d(TAG, "successful file creation");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(imageAttachment));
                startActivityForResult(takePictureIntent, Constants.IMAGE_CAPTURE_REQUEST);
            }
        }
        else{
            Toast.makeText(mActivity, R.string.no_camera_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void takeVideo(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, Constants.VIDEO_CAPTURE_REQUEST);
        }
        else{
            Toast.makeText(mActivity, R.string.no_camera_app, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.d(TAG, "onActivityResult");
        if (requestCode == Constants.SEARCH_EVENT_REQUEST) {
            // Make sure the request was successful
            Log.d(TAG, "SEARCH_EVENT_REQUEST");
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "RESULT_OK");
                Event e = data.getParcelableExtra(Constants.EVENT);
                postEventToCurrentChannel(e);
            }
        } else if (requestCode == Constants.CREATE_EVENT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "RESULT_OK");
                Event e = data.getParcelableExtra(Constants.EVENT);
                postEventToCurrentChannel(e);
            }
        } else if(requestCode == Constants.IMAGE_CAPTURE_REQUEST){
            // Create an image file name
            Log.d(TAG, "getting camera picture");
            if (resultCode == Activity.RESULT_OK) {
                sendFile(imageAttachment, "Image partagée par "+activeUser.getFirstame(), MediaType.parse("image/jpg"), Message.IMAGE);
            }
        }
        else if(requestCode == Constants.VIDEO_CAPTURE_REQUEST){
            Log.d(TAG, "getting camera video");
            if(resultCode == Activity.RESULT_OK){
                Uri videoUri = data.getData();
                sendFile(new File(io.goodway.infotel.utils.File.getPath(mActivity, videoUri)),"Vidéo partagée par "+activeUser.getFirstame(), MediaType.parse(io.goodway.infotel.utils.File.getMimeType(mActivity, videoUri)), Message.VIDEO);
            }
        }
        else if (requestCode == Constants.LOAD_IMAGE_REQUEST) {
            Log.d(TAG, "LOAD_IMAGE_REQUEST");
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri imageUri = data.getData();
                sendFile(new File(io.goodway.infotel.utils.File.getPath(mActivity, imageUri)), "Image partagée par "+activeUser.getFirstame(), MediaType.parse(io.goodway.infotel.utils.File.getMimeType(mActivity, imageUri)), Message.IMAGE);
            }
            else{
                Log.d(TAG, "Data null");
            }
        }
    }
    public void sendFile(File f, final String message, MediaType mediaType, final int attachmentType){
        final ProgressDialog pd = new ProgressDialog(mActivity);
        pd.setTitle("Envoi du fichier");
        pd.setMessage("En cours, veuillez patienter");
        pd.setIndeterminate(true);
        pd.show();
        HttpRequest.upload(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "failure");
                Log.d(TAG, e.toString());
                e.printStackTrace();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.hide();
                        Toast.makeText(mActivity, "echec de l'envoi du fichier", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Log.d(TAG, "code="+response.code());
                if(response.code()==201){
                    try {
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        final String file = jsonResult.optString("file");
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.hide();
                                ChatFragment.this.sendMessage(new Message(activeUser, message, attachmentType, "http://infotel.goodway.io/api/uploads/" + file, true));
                            }
                        });
                    } catch (final Exception e) {
                        Log.d(TAG, e.getMessage());
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.hide();
                                Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
            }
        }, f, mediaType);
    }
}