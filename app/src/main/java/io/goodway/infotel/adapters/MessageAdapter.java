package io.goodway.infotel.adapters;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import io.goodway.infotel.R;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.utils.Image;

/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> mDataset;
    private Context context;
    private Callback callback;
    private RecyclerView recyclerView;

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public MessageAdapter(Context context, RecyclerView recyclerView, Callback<Message> callback) {
        mDataset = new ArrayList<Message>();
        this.context = context;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Message m = mDataset.get(position);
        Log.d("content", m.getContent());
        int viewType = getItemViewType(position);
        Log.d("viewType", "viewType="+viewType);
        switch (viewType){
            case Message.TEXT:
                MessageViewHolder mHolder = (MessageViewHolder)holder;
                mHolder.content.setText(m.getContent());
                if(m.from_me()){
                    mHolder.avatar.setVisibility(View.INVISIBLE);
                    mHolder.mainLayout.setGravity(Gravity.RIGHT);
                }
                else{
                    mHolder.avatar.setVisibility(View.VISIBLE);
                    mHolder.mainLayout.setGravity(Gravity.LEFT);
                    Log.d(TAG, "icon="+m.getAvatar());

                    if(position!=0 && mDataset.get(position-1).getSender_id()!=m.getSender_id()){
                        Picasso.with(context)
                                .load(m.getAvatar())
                                .error(R.mipmap.ic_image_black_24dp)
                                .fit().centerCrop()
                                .transform(new Image.ImageTransCircleTransform())
                                .into(mHolder.avatar);
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
                    Picasso.with(context)
                            .load(m.getAvatar())
                            //.error(R.mipmap)
                            .fit().centerCrop()
                            .transform(new Image.ImageTransCircleTransform())
                            .into(imHolder.avatar);
                }

                Picasso.with(context)
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

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getAttachment_type();
    }

    public void add(Message item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(0, mDataset.size());
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
