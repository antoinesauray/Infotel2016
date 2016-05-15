package io.goodway.infotel.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.goodway.infotel.R;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.communication.Channel;

/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class SendingChannelAdapter extends RecyclerView.Adapter<SendingChannelAdapter.ChannelHolder> {

    private List<Channel> mDataset;
    private Context context;
    private Callback callback;
    private HashMap<Integer, Channel> selectedPositions;

    private static final String TAG="ChannelAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public SendingChannelAdapter(Context context, Callback<Channel> callback) {
        mDataset = new ArrayList<Channel>();
        this.context = context;
        this.callback = callback;
        selectedPositions = new HashMap<Integer, Channel>();
    }

    public Collection<Channel> getSelectedChannels(){
        return selectedPositions.values();
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
        Channel c = mDataset.get(position);
        holder.setItem(c);
        holder.name.setText("# "+c.getName());
        if(selectedPositions.containsKey(position)){
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            holder.name.setTextColor(context.getResources().getColor(android.R.color.white));
        }

        else{
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.name.setTextColor(context.getResources().getColor(android.R.color.primary_text_light));
        }
    }


    public void add(Channel item) {
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
            int position = getLayoutPosition();
            if(selectedPositions.containsKey(position)){
                selectedPositions.remove(getLayoutPosition());
            }
            else{
                selectedPositions.put(position, item);
            }
            notifyItemChanged(position);
        }
    }

}
