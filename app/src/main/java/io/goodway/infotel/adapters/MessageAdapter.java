package io.goodway.infotel.adapters;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.goodway.infotel.R;
import io.goodway.infotel.activities.PdfActivity;
import io.goodway.infotel.callbacks.Callback;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.File;
import io.goodway.infotel.utils.Image;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @see android.widget.ArrayAdapter
 * @author Antoine Sauray
 * @version 1.0
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Message> mDataset;
    private Activity activity;
    private Callback callback;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    private HashMap<Integer, String> avatars;

    private static final String TAG = "MessageAdapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public MessageAdapter(Activity activity, RecyclerView recyclerView, LinearLayoutManager layoutManager, Callback<Message> callback) {
        mDataset = new ArrayList<Message>(){
            public boolean add(Message mt) {
                super.add(mt);
                Collections.sort(mDataset);
                return true;
            }
        };
        this.layoutManager = layoutManager;
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
        switch (viewType) {
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
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_pdf, parent, false);
                break;
            case Message.VIDEO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_video, parent, false);
                return new VideoMessageViewHolder((LinearLayout) v);
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
        Log.d("viewType", "viewType=" + viewType);
        switch (viewType) {
            case Message.TEXT:
                final MessageViewHolder mHolder = (MessageViewHolder) holder;
                mHolder.content.setText(m.getContent());
                if (m.from_me()) {
                    mHolder.avatar.setVisibility(View.INVISIBLE);
                    mHolder.mainLayout.setGravity(Gravity.RIGHT);
                } else {
                    mHolder.avatar.setVisibility(View.VISIBLE);
                    mHolder.mainLayout.setGravity(Gravity.LEFT);
                    if (position != 0) {
                        Log.d(TAG, "previous sender id=" + mDataset.get(position - 1).getSender_id());
                    }
                    Log.d(TAG, "sender id=" + m.getSender_id());
                    findAndSetAvatar(position, m, mHolder);
                }
                break;
            case Message.IMAGE:
                ImageMessageViewHolder imHolder = (ImageMessageViewHolder) holder;
                imHolder.content.setText(m.getContent());
                if (m.from_me()) {
                    imHolder.avatar.setVisibility(View.INVISIBLE);
                    imHolder.mainLayout.setGravity(Gravity.RIGHT);
                } else {
                    //Log.d("displaying image", "url="+m.getAttachment());
                    imHolder.avatar.setVisibility(View.VISIBLE);
                    imHolder.mainLayout.setGravity(Gravity.LEFT);
                    findAndSetAvatar(position, m, imHolder);
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
                final PdfMessageViewHolder pdfHolder = (PdfMessageViewHolder) holder;
                pdfHolder.content.setText(m.getContent());
                if (m.from_me()) {
                    pdfHolder.avatar.setVisibility(View.INVISIBLE);
                    pdfHolder.mainLayout.setGravity(Gravity.RIGHT);
                } else {
                    pdfHolder.avatar.setVisibility(View.VISIBLE);
                    pdfHolder.mainLayout.setGravity(Gravity.LEFT);
                    if (position != 0) {
                        Log.d(TAG, "previous sender id=" + mDataset.get(position - 1).getSender_id());
                    }
                    Log.d(TAG, "sender id=" + m.getSender_id());
                    findAndSetAvatar(position, m, pdfHolder);
                    pdfHolder.attachment.setText(File.getNameFromUrl(m.getAttachment()));
                }
                break;
            case Message.VIDEO:
                VideoMessageViewHolder vmHolder = (VideoMessageViewHolder) holder;
                vmHolder.content.setText(m.getContent());
                if (m.from_me()) {
                    vmHolder.avatar.setVisibility(View.INVISIBLE);
                    vmHolder.mainLayout.setGravity(Gravity.RIGHT);
                } else {
                    vmHolder.avatar.setVisibility(View.VISIBLE);
                    vmHolder.mainLayout.setGravity(Gravity.LEFT);
                    findAndSetAvatar(position, m, vmHolder);
                    vmHolder.attachment.setVideoPath(m.getAttachment());
                    vmHolder.attachment.setMediaController(new MediaController(activity)); //sets MediaController in the video view
                    vmHolder.attachment.start();
                }

                break;
            default:

                break;
        }
    }

    public void setAvatar(final ImageView imageView, final String url) {
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

    public void findAndSetAvatar(final int position, final Message m, final AvatarViewHolder messageViewHolder) {

        if (avatars.containsKey(m.getSender_id())) {
            if (position == 0) {
                setAvatar(messageViewHolder.avatar, avatars.get(m.getSender_id()));
            } else if (mDataset.get(position - 1).getSender_id() != m.getSender_id() && !avatars.get(m.getSender_id()).isEmpty()) {
                setAvatar(messageViewHolder.avatar, avatars.get(m.getSender_id()));
            }
        } else {
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
                            Log.d(TAG, "avatar=" + avatar);
                            avatars.put(m.getSender_id(), avatar);
                            if (position == 0) {
                                if (!avatars.get(m.getSender_id()).isEmpty()) {
                                    setAvatar(messageViewHolder.avatar, avatars.get(m.getSender_id()));
                                }
                            } else if (mDataset.get(position - 1).getSender_id() != m.getSender_id() && !avatar.isEmpty()) {
                                setAvatar(messageViewHolder.avatar, avatars.get(m.getSender_id()));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, m.getSender_id());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getAttachment_type();
    }

    public void add(Message item) {
        if (recyclerView.getChildCount() == mDataset.size()){
            layoutManager.setStackFromEnd(true);
        }
        else{
            layoutManager.setStackFromEnd(false);
        }
        int position = mDataset.size();
        mDataset.add(position, item);
        notifyItemInserted(position);
        recyclerView.scrollToPosition(position);
    }

    public void setMessages(ArrayList<Message> items) {
        this.mDataset = items;
        try {
            notifyItemRangeChanged(0, mDataset.size());
        }
        catch(IllegalStateException e){}
        Log.d(TAG, items.toString());
    }

    public void clear() {
        int size = mDataset.size();
        mDataset.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class MessageViewHolder extends AvatarViewHolder implements View.OnLongClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        LinearLayout mainLayout;
        TextView content;

        public MessageViewHolder(LinearLayout mainLayout) {
            super(mainLayout);
            this.mainLayout = mainLayout;
            mainLayout.setOnLongClickListener(this);
            content = (TextView) mainLayout.findViewById(R.id.content);
        }

        public void setItem(Message item) {
            this.item = item;
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    public class ImageMessageViewHolder extends AvatarViewHolder implements View.OnLongClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        LinearLayout mainLayout;
        TextView content;
        ImageView attachment;

        public ImageMessageViewHolder(LinearLayout mainLayout) {
            super(mainLayout);
            this.mainLayout = mainLayout;
            mainLayout.setOnLongClickListener(this);
            content = (TextView) mainLayout.findViewById(R.id.content);
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

    public class VideoMessageViewHolder extends AvatarViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        LinearLayout mainLayout;
        TextView content;
        VideoView attachment;

        public VideoMessageViewHolder(LinearLayout mainLayout) {
            super(mainLayout);
            this.mainLayout = mainLayout;
            mainLayout.setOnClickListener(this);
            content = (TextView) mainLayout.findViewById(R.id.content);
            attachment = (VideoView) mainLayout.findViewById(R.id.attachment);
        }

        public void setItem(Message item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            attachment.start();
        }
    }

    public class PdfMessageViewHolder extends AvatarViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        LinearLayout mainLayout;
        TextView content;
        TextView attachment;

        public PdfMessageViewHolder(LinearLayout mainLayout) {
            super(mainLayout);
            this.mainLayout = mainLayout;
            mainLayout.setOnClickListener(this);
            content = (TextView) mainLayout.findViewById(R.id.content);
            attachment = (TextView) mainLayout.findViewById(R.id.attachment);
            attachment.setPaintFlags(attachment.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        public void setItem(Message item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(activity, PdfActivity.class);
            i.putExtra(Constants.ATTACHMENT, item.getAttachment());
            activity.startActivity(i);
        }
    }

    public class FileMessageViewHolder extends AvatarViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        LinearLayout mainLayout;
        TextView content;
        TextView attachment;

        public FileMessageViewHolder(LinearLayout mainLayout) {
            super(mainLayout);
            this.mainLayout = mainLayout;
            mainLayout.setOnClickListener(this);
            content = (TextView) mainLayout.findViewById(R.id.content);
            attachment = (TextView) mainLayout.findViewById(R.id.attachment);
            attachment.setPaintFlags(attachment.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        public void setItem(Message item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            final File.DownloadTask downloadTask = new File.DownloadTask(activity, File.getNameFromUrl(item.getAttachment()), null, new HttpRequest.Action<java.io.File>() {
                @Override
                public void action(java.io.File file) {
                    final NotificationManager mNotification = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);

                    Intent launchNotifiactionIntent = new Intent();
                    launchNotifiactionIntent.setAction(android.content.Intent.ACTION_VIEW);

                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                    String type = mime.getMimeTypeFromExtension(ext);

                    launchNotifiactionIntent.setDataAndType(Uri.fromFile(file), type);

                    final PendingIntent pendingIntent = PendingIntent.getActivity(activity,
                            Constants.DOWNLOAD_FILE, launchNotifiactionIntent,
                            PendingIntent.FLAG_ONE_SHOT);

                    Notification.Builder builder = new Notification.Builder(activity)
                            .setWhen(System.currentTimeMillis())
                            .setTicker(activity.getString(R.string.open_file))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(activity.getResources().getString(R.string.file_downloaded))
                            .setContentText(activity.getResources().getString(R.string.open))
                            .setContentIntent(pendingIntent);

                    mNotification.notify(0, builder.build());
                }
            });
            downloadTask.execute(item.getAttachment());
        }
    }


    public class YoutubeMessageViewHolder extends AvatarViewHolder implements View.OnClickListener, YouTubePlayer.OnInitializedListener {
        // each data item is just a string in this case
        //TextView s_name, a_name;
        Message item;
        TextView content;
        YouTubePlayerView attachment;
        YouTubePlayer youTubePlayer;

        public YoutubeMessageViewHolder(View root) {
            super(root);
            root.setOnClickListener(this);
            //content = (TextView) mainLayout.findViewById(R.id.content);
            //attachment = (YouTubePlayerView) mainLayout.findViewById(R.id.attachment);
            //attachment.initialize(Constants.API_KEY, this);
        }

        public void setItem(Message item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            play(item.getAttachment());
        }

        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
            this.youTubePlayer=youTubePlayer;
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        }

        void play(String id){
            this.youTubePlayer.cueVideo(id);
        }
    }

    public class AvatarViewHolder extends RecyclerView.ViewHolder {
        protected ImageView avatar;
        public AvatarViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }

}

