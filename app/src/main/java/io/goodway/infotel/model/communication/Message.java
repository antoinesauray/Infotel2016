package io.goodway.infotel.model.communication;

import java.util.GregorianCalendar;

/**
 * Created by antoine on 5/11/16.
 */
public class Message {

    public static final int MESSAGE=1, IMAGE=2, MUSIC=3, PDF=4;

    public int getSender_id() {
        return sender_id;
    }

    private int sender_id;

    public String getContent() {
        return content;
    }

    private String content;

    public String getAttachment_url() {
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

    public Message(int sender_id, String content, int attachment_type, String attachment_url, boolean from_me){
        this.sender_id=sender_id;
        this.content = content;
        this.attachment_url =attachment_url;
        this.attachment_type = attachment_type;
        this.from_me = from_me;
    }

}
