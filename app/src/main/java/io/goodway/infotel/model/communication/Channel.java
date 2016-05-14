package io.goodway.infotel.model.communication;

/**
 * Created by antoine on 5/14/16.
 */
public class Channel {

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
}
