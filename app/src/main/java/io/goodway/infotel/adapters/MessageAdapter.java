package io.goodway.infotel.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.goodway.infotel.R;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.Image;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> mDataset;
    private Activity activity;
    private Callback callback;
    private RecyclerView recyclerView;

    private HashMap<Integer, String> avatars;

    private static final String TAG="MessageAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public MessageAdapter(Activity activity, RecyclerView recyclerView, Callback<Message> callback) {
        mDataset = new ArrayList<Message>();
        avatars = new HashMap<>();
        this.activity = activity;
        this.callback = callback;
        this.recyclerView = recyclerView;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view

        View v = null;
        switch (viewType){
            case Message.TEXT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);
                return new MessageViewHolder((LinearLayout) v);
            case Message.IMAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_image, parent, false);
                return new ImageMessageViewHolder((LinearLayout) v);
            case Message.MUSIC:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);
                break;
            case Message.PDF:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);
                break;
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);
                break;
        }
        return new MessageViewHolder((LinearLayout) v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Message m = mDataset.get(position);
        Log.d("content", m.getContent());
        int viewType = getItemViewType(position);
        Log.d("viewType", "viewType="+viewType);
        switch (viewType){
            case Message.TEXT:
                final MessageViewHolder mHolder = (MessageViewHolder)holder;
                mHolder.content.setText(m.getContent());
                if(m.from_me()){
                    mHolder.avatar.setVisibility(View.INVISIBLE);
                    mHolder.mainLayout.setGravity(Gravity.RIGHT);
                }
                else {
                    mHolder.avatar.setVisibility(View.VISIBLE);
                    mHolder.mainLayout.setGravity(Gravity.LEFT);
                    if(position!=0){Log.d(TAG, "previous sender id=" + mDataset.get(position - 1).getSender_id());}
                    Log.d(TAG, "sender id=" + m.getSender_id());

                    if(avatars.containsKey(m.getSender_id())){
                        if(position==0){
                            setAvatar(mHolder.avatar, avatars.get(m.getSender_id()));
                        }
                        else if (mDataset.get(position - 1).getSender_id() != m.getSender_id() && !avatars.get(m.getSender_id()).isEmpty()) {
                            setAvatar(mHolder.avatar, avatars.get(m.getSender_id()));
                        }
                    }
                    else{
                        HttpRequest.user(new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.code() == 200) {
                                    JSONObject jsonResult = null;
                                    try {
                                        jsonResult = new JSONObject(response.body().string());
                                        JSONObject user = jsonResult.optJSONObject("user");
                                        String avatar = user.optString("avatar");
                                        Log.d(TAG, "avatar="+avatar);
                                        avatars.put(m.getSender_id(), avatar);
                                        if(position == 0){
                                            if(!avatars.get(m.getSender_id()).isEmpty()) {
                                                setAvatar(mHolder.avatar, avatars.get(m.getSender_id()));
                                            }
                                        }
                                        else if (mDataset.get(position - 1).getSender_id() != m.getSender_id() && !avatar.isEmpty()) {
                                            setAvatar(mHolder.avatar, avatars.get(m.getSender_id()));
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, m.getSender_id());
                    }
                }
                break;
            case Message.IMAGE:
                ImageMessageViewHolder imHolder = (ImageMessageViewHolder)holder;
                imHolder.content.setText(m.getContent());
                if(m.from_me()){
                    imHolder.avatar.setVisibility(View.INVISIBLE);
                    imHolder.mainLayout.setGravity(Gravity.RIGHT);
                }
                else{
                    //Log.d("displaying image", "url="+m.getAttachment());
                    imHolder.avatar.setVisibility(View.VISIBLE);
                    imHolder.mainLayout.setGravity(Gravity.LEFT);
                   //setAvatar(imHolder.avatar, );
                }

                Picasso.with(activity)
                        .load(m.getAttachment())
                        .error(R.mipmap.ic_image_black_24dp)
                        .fit().centerInside()
                        .into(imHolder.attachment);

                break;
            case Message.MUSIC:
                break;
            case Message.PDF:
                break;
            default:
                Log.d("shit", "shit");
                break;
        }
    }

    public void setAvatar(final ImageView imageView, final String url){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(MessageAdapter.this.activity)
                        .load(url)
                        //.error(R.mipmap)
                        .fit().centerCrop()
                        .transform(new Image.ImageTransCircleTransform())
                        .into(imageView);
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getAttachment_type();
    }

    public void add(Message item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
        //notifyItemRangeChanged(0, mDataset.size());
        recyclerView.scrollToPosition(position);
    }

    public void clear(){
        int size = mDataset.size();
        mDataset.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        LinearLayout mainLayout;
        TextView content;
        ImageView avatar;

        public MessageViewHolder(LinearLayout mainLayout) {
            super(mainLayout);
            this.mainLayout = mainLayout;
            mainLayout.setOnLongClickListener(this);
            content = (TextView) mainLayout.findViewById(R.id.content);
            avatar = (ImageView) mainLayout.findViewById(R.id.avatar);
        }

        public void setItem(Message item) {
            this.item = item;
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    public class ImageMessageViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        LinearLayout mainLayout;
        TextView content;
        ImageView avatar, attachment;

        public ImageMessageViewHolder(LinearLayout mainLayout) {
            super(mainLayout);
            this.mainLayout=mainLayout;
            mainLayout.setOnLongClickListener(this);
            content = (TextView) mainLayout.findViewById(R.id.content);
            avatar = (ImageView) mainLayout.findViewById(R.id.avatar);
            attachment = (ImageView) mainLayout.findViewById(R.id.attachment);
        }

        public void setItem(Message item) {
            this.item = item;
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
}
