package io.goodway.infotel.model.communication;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by antoine on 5/14/16.
 */
public class Channel implements Parcelable {

    protected Channel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        fullname = in.readString();
        avatar = in.readString();
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public int getId() {
        return id;
    }

    private int id;

    public String getName() {
        return name;
    }

    private String name;

    public String getFullName() {
        return fullname;
    }

    private String fullname;


    public String getAvatar() {
        return avatar;
    }

    private String avatar;

    public Channel(int id, String name, String fullname, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.fullname = fullname;
    }

    private int pendingMessages=0;

    public void incrementPendingMessages(){this.pendingMessages++;}

    public int getPendingMessages(){return pendingMessages;}

    public void resetPendingMessage(){pendingMessages=0;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(fullname);
        dest.writeString(avatar);
    }
}
