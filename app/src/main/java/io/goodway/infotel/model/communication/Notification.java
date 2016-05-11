package io.goodway.infotel.model.communication;

/**
 * Created by antoine on 5/11/16.
 */
public class Notification {

    public String getTitle() {
        return title;
    }

    private String title;

    public String getIcon() {
        return icon;
    }

    private String icon;

    public String getContent() {
        return content;
    }

    private String content;

    public Notification(String title, String icon, String content){
        this.title = title;
        this.icon = icon;
        this.content = content;
    }

}
