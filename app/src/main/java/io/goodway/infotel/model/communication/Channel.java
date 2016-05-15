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

    public String getAvatar() {
        return avatar;
    }

    private String avatar;

    public Channel(int id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(avatar);
    }
}
