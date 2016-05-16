package io.goodway.infotel.model;


import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by antoine on 5/16/16.
 */
public class Event implements Parcelable{

    private String name;
    private DateTime date_start, date_end;
    private String avatar;
    private int id;

    public int getId() {
        return id;
    }

    private int type;

    public Event(int id, String name, DateTime date_start, DateTime date_end, String avatar, int type, double place_lat_start, double place_lon_start, double place_lat_end, double place_lon_end, int user_id) {
        this.id = id;
        this.name = name;
        this.date_start = date_start;
        this.date_end = date_end;
        this.avatar = avatar;
        this.type = type;
        this.place_lat_start = place_lat_start;
        this.place_lon_start = place_lon_start;
        this.place_lat_end = place_lat_end;
        this.place_lon_end = place_lon_end;
        this.user_id = user_id;
    }

    public Event(Parcel in){
        id = in.readInt();
        name = in.readString();
        avatar = in.readString();
        type = in.readInt();
        place_lat_start = in.readDouble();
        place_lon_start = in.readDouble();
        place_lat_end = in.readDouble();
        place_lon_end = in.readDouble();
        user_id = in.readInt();
    }

    private double place_lat_start;

    public double getPlace_lon_start() {
        return place_lon_start;
    }

    public DateTime getDate_start() {
        return date_start;
    }

    public DateTime getDate_end() {
        return date_end;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getType() {
        return type;
    }

    public double getPlace_lat_start() {
        return place_lat_start;
    }

    public double getPlace_lat_end() {
        return place_lat_end;
    }

    public double getPlace_lon_end() {
        return place_lon_end;
    }

    public void setId(int id){this.id = id;}


    private double place_lon_start;
    private double place_lat_end;
    private double place_lon_end;
    private int user_id;

    public static final int CARPOOLING=1, RUNNING=2, AFTERWORK=3, INFOTEL=4;

    public String getName() {
        return name;
    }

    public int getUser_id() {
        return user_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeSerializable(date_start);
        dest.writeSerializable(date_end);
        dest.writeString(avatar);
        dest.writeInt(type);
        dest.writeDouble(place_lat_start);
        dest.writeDouble(place_lon_start);
        dest.writeDouble(place_lat_end);
        dest.writeDouble(place_lat_end);
        dest.writeInt(user_id);
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

}
