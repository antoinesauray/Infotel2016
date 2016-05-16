package io.goodway.infotel.model;


import java.util.Date;

/**
 * Created by antoine on 5/16/16.
 */
public class Event {

    private String name;
    private Date date_start, date_end;
    private String avatar;
    private int type;

    public Event(String name, Date date_start, Date date_end, String avatar, int type, double place_lat_start, double place_lon_start, double place_lat_end, double place_lon_end, int user_id) {
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

    private double place_lat_start;

    public double getPlace_lon_start() {
        return place_lon_start;
    }

    public Date getDate_start() {
        return date_start;
    }

    public Date getDate_end() {
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

    public static int getCARPOOLING() {
        return CARPOOLING;
    }

    public static int getRUNNING() {
        return RUNNING;
    }

    public static int getAFTERWORK() {
        return AFTERWORK;
    }

    private double place_lon_start;
    private double place_lat_end;
    private double place_lon_end;
    private int user_id;

    public static int CARPOOLING=1, RUNNING=2, AFTERWORK=3;

    public String getName() {
        return name;
    }

    public int getUser_id() {
        return user_id;
    }

}
