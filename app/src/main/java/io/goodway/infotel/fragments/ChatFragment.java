package io.goodway.infotel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import io.goodway.infotel.R;
import io.goodway.infotel.adapters.MessageAdapter;
import io.goodway.infotel.model.communication.Message;

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

        adapter = new MessageAdapter(recyclerView, null);


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        message.addTextChangedListener(this);
        send.setOnClickListener(this);

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
            adapter.add(new Message(1, content, 1, null, true));
            message.getText().clear();
        }
    }
}