package io.goodway.infotel.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.flipboard.bottomsheet.BottomSheetLayout;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.MessageAdapter;
import io.goodway.infotel.model.Event;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.sync.gcm.GCMService;
import io.goodway.infotel.utils.Constants;
import okhttp3.Call;
import okhttp3.Callback;
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
                bottomSheet.showWithSheetView(LayoutInflater.from(getContext()).inflate(R.layout.view_context_post, bottomSheet, false));
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
}