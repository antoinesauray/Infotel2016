package io.goodway.infotel.model.communication;

/**
 * Created by antoine on 5/14/16.
 */
public class Subscription {

    private int id;

    public int getId() {
        return id;
    }

    public int getChannel_id() {
        return channel_id;
    }

    public int getUser_id() {
        return user_id;
    }

    private int channel_id;
    private int user_id;

    public Subscription(int id, int channel_id, int user_id) {
        this.id = id;
        this.channel_id = channel_id;
        this.user_id = user_id;
    }

}
