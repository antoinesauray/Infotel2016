package io.goodway.infotel.model.communication;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.GregorianCalendar;

import io.goodway.infotel.model.User;

/**
 * Created by antoine on 5/11/16.
 */
public class Message implements Parcelable {

    public static final int TEXT =0, IMAGE=1, FILE=2, MUSIC=3, PDF=4, VIDEO=5;

    public int getSender_id() {
        return sender_id;
    }

    private int sender_id;

    public String getContent() {
        return content;
    }

    private String content;

    public String getName() {
        return name;
    }

    private String name;

    public String getAvatar() {
        return avatar;
    }

    private String avatar;

    public String getAttachment() {
        return attachment_url;
    }

    private String attachment_url;

    public int getAttachment_type() {
        return attachment_type;
    }

    private int attachment_type;

    public boolean from_me() {
        return from_me;
    }

    private boolean from_me;

    private GregorianCalendar date;

    public Message(int sender_id, String name, String avatar, String content, int attachment_type, String attachment_url, boolean from_me){
        this.sender_id=sender_id;
        this.name=name;
        this.avatar=avatar;
        this.content = content;
        this.attachment_url =attachment_url;
        this.attachment_type = attachment_type;
        this.from_me = from_me;
    }

    public Message(User user,String content, int attachment_type, String attachment_url, boolean from_me){
        this.sender_id=user.getId();
        this.name=user.getFirstame();
        this.avatar=user.getAvatar();
        this.content = content;
        this.attachment_url =attachment_url;
        this.attachment_type = attachment_type;
        this.from_me = from_me;
    }

    public Message(Parcel in){
        sender_id = in.readInt();
        name = in.readString();
        avatar = in.readString();
        content = in.readString();
        attachment_type = in.readInt();
        attachment_url = in.readString();
        Log.d("PARCEL url=", "url="+attachment_url);
        from_me = in.readByte() != 0;
    }

    @Override
    public String toString(){
        return "{"+sender_id+"}:{"+content+"}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sender_id);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(content);
        dest.writeInt(attachment_type);
        dest.writeString(attachment_url);
        dest.writeByte((byte) (from_me ? 1 : 0));
    }

    public static final Creator CREATOR =
            new Creator() {
                public Message createFromParcel(Parcel in) {
                    return new Message(in);
                }

                public Message[] newArray(int size) {
                    return new Message[size];
                }
            };

}
