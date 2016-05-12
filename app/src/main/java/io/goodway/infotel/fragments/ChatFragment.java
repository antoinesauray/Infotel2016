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

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.MessageAdapter;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpClient;
import io.goodway.infotel.sync.gcm.GCMService;
import io.goodway.infotel.sync.gcm.QuickstartPreferences;

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

    private int sender_id=1;

    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Message message = intent.getParcelableExtra("message");
            if(message.getSender_id()!=sender_id){
                Log.d("receiver", "Got message: " + message.getContent());
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

        recyclerView = (RecyclerView) root.findViewById(R.id.list);
        send = (ImageButton) root.findViewById(R.id.send);
        message = (EditText) root.findViewById(R.id.message);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        layoutManager = new LinearLayoutManager(getContext());

        adapter = new MessageAdapter(getContext(), recyclerView, null);


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        message.addTextChangedListener(this);
        send.setOnClickListener(this);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(GCMService.MESSAGE_RECEIVED));

        return root;
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
            final Message m = new Message(sender_id, content, Message.MESSAGE, null, true);
            adapter.add(m);
            message.getText().clear();
            HttpClient.Post.messageInTopic(getContext(), new HttpClient.Action<Boolean>() {
                @Override
                public void action(Boolean e) {
                    if(!e){
                        Toast.makeText(ChatFragment.this.getContext(), "Impossible d'envoyer le message", Toast.LENGTH_SHORT).show();
                    }
                }
            }, m, "global");
        }
    }
}