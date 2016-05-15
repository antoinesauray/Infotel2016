package io.goodway.infotel.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.MessageAdapter;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.sync.gcm.GCMService;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.Debug;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class ChatFragment extends Fragment implements TextWatcher, View.OnClickListener {

    private View root;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ImageButton send;
    private EditText message;

    private MessageAdapter adapter;

    private String name="Antoine";
    private String avatar="http://data.goodway.io/img/alexis.jpg";

    private String TAG = "ChatFragment";

    private Channel activeChannel;
    private User activeUser;

    private View selectChannel;

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
            Log.d(TAG, "active_channel_id="+activeChannel.getId());

            if(channel_id>-1 && channel_id==activeChannel.getId() && message.getSender_id()!= activeUser.getId()){
            Log.d("displaying type", "type="+message.getAttachment_type());
            Log.d("displaying image", "url="+message.getAttachment());
                adapter.add(message);
            }
        }
    };

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
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

        activeUser = getArguments().getParcelable(Constants.USER);

        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        send = (ImageButton) root.findViewById(R.id.send);
        message = (EditText) root.findViewById(R.id.message);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(getContext());

        adapter = new MessageAdapter(getActivity(), recyclerView, null);


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        message.addTextChangedListener(this);
        send.setOnClickListener(this);

        selectChannel = root.findViewById(R.id.select_channel);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(GCMService.MESSAGE_RECEIVED));

        //activeChannel = new Channel(1, "Général");
        return root;
    }

    public void onResume(){
        super.onResume();
        switchChannel(activeChannel);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(s.length()>0 && !send.isEnabled()){
            send.setEnabled(true);
        }
        else if(s.length()==0 && send.isEnabled()){
            send.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        String content = message.getText().toString();
        if(content.length()>0){
            final Message m = new Message(activeUser.getId(), name, avatar, content, Message.TEXT, "http://www.infotel.com/wp-content/uploads/2013/03/logo.png", true);
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
    }

    public void switchChannel(final Channel channel){
        if(channel!=null) {
            activeChannel = channel;
            Log.d(TAG, "switching to channel " + channel.getName());
            selectChannel.setVisibility(View.GONE);
            message.setEnabled(true);
            send.setEnabled(true);
            adapter.clear();
            HttpRequest.messages(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

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
                            for (int i = 0; i < length; i++) {
                                final JSONObject obj = channels.optJSONObject(i);
                                final int sender_id = obj.optInt("user_id");
                                Log.d(TAG, "sender_id="+sender_id);
                                Log.d(TAG, "active_sender_id="+activeUser.getId());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(new Message(
                                                sender_id,
                                                obj.optString("name"),
                                                obj.optString("avatar"),
                                                obj.optString("content"),
                                                obj.optInt("attachment_type"),
                                                obj.optString("attachment"),
                                                sender_id == activeUser.getId()
                                        ));
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, channel);
        }
    }
}