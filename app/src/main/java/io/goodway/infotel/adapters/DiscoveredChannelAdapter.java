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
import java.util.List;

import io.goodway.infotel.R;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.utils.Image;

/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class DiscoveredChannelAdapter extends RecyclerView.Adapter<DiscoveredChannelAdapter.ChannelHolder> {

    private List<Channel> mDataset;
    private Context context;
    private Callback callback;

    private static final String TAG="DiscoveredChannelAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public DiscoveredChannelAdapter(Context context, Callback<Channel> callback) {
        mDataset = new ArrayList<Channel>();
        this.context = context;
        this.callback = callback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChannelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_discovered_channel, parent, false);
        return new ChannelHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ChannelHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Channel c = mDataset.get(position);
        holder.setItem(c);
        holder.name.setText(c.getName());

        if(c.getAvatar()!=null && !c.getAvatar().isEmpty()){
            Picasso.with(context)
                    .load(c.getAvatar())
                    .error(R.mipmap.ic_public_black_36dp)
                    .fit().centerCrop()
                    .transform(new Image.ImageTransCircleTransform())
                    .into(holder.icon);
        }
        else{
            holder.icon.setBackgroundResource(R.mipmap.ic_public_black_36dp);
        }

    }


    public void add(List<Channel> items) {
        int start = mDataset.size();
        mDataset.addAll(items);
        notifyItemRangeInserted(start, mDataset.size());
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
        Channel item;
        TextView name;
        ImageView icon;
        View root;

        public ChannelHolder(View root) {
            super(root);
            this.root=root;
            root.setOnClickListener(this);
            name = (TextView) root.findViewById(R.id.name);
            icon = (ImageView) root.findViewById(R.id.icon);
        }

        public void setItem(Channel item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            callback.callback(item);
        }
    }

}
