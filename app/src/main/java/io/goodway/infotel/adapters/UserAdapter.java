package io.goodway.infotel.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.goodway.infotel.R;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.User;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.utils.Image;

/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ChannelHolder> {

    private List<User> mDataset;
    private Context context;
    private Callback callback;
    private int selectedPos=-1; // -1 allows to not start in a chat. 0 would make the client start within first chat

    private static final String TAG="LINE_ADAPTER";

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserAdapter(Context context, Callback<Channel> callback) {
        mDataset = new ArrayList<User>();
        this.context = context;
        this.callback = callback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChannelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_channel, parent, false);
        return new ChannelHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ChannelHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        User c = mDataset.get(position);
        holder.setItem(c);

        Picasso.with(context)
                .load(c.getAvatar())
                //.error(R.mipmap)
                .fit().centerInside()
                .transform(new Image.ImageTransCircleTransform())
                .into(holder.avatar);
        Log.d("selectedPos", "pos="+selectedPos);
        Log.d("position", "pos="+position);

    }


    public void add(User item) {
        Log.d(TAG, "adding Channel: "+item);
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
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

    public class ChannelHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        User item;
        TextView name;
        ImageView avatar;
        View root;

        public ChannelHolder(View root) {
            super(root);
            this.root=root;
            root.setOnClickListener(this);
            avatar = (ImageView) root.findViewById(R.id.avatar);
        }

        public void setItem(User item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            callback.callback(item);
        }
    }

}
