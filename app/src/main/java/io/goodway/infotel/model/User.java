package io.goodway.infotel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.GregorianCalendar;

/**
 * Created by antoine on 5/11/16.
 */
public class User implements Parcelable{

    private int id;

    public String getFirstame() {
        return firstame;
    }

    public int getId() {
        return id;
    }

    public String getLastname() {
        return lastname;
    }

    public String getMail() {
        return mail;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getToken() {
        return token;
    }

    public static Creator getCREATOR() {
        return CREATOR;
    }

    private String firstame, lastname, mail, avatar, token;

    public User(int id, String firstame, String lastname, String mail, String avatar, String token){
        this.id = id;
        this.firstame = firstame;
        this.lastname = lastname;
        this.mail = mail;
        this.avatar = avatar;
        this.token = token;
    }

    public User(Parcel in){
        id = in.readInt();
        firstame = in.readString();
        lastname = in.readString();
        mail = in.readString();
        avatar = in.readString();
        token = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(firstame);
        dest.writeString(lastname);
        dest.writeString(mail);
        dest.writeString(avatar);
        dest.writeString(token);
    }

    @Override
    public String toString(){
        return "{"+firstame+","+lastname+","+mail+","+avatar+"}";
    }

    public static final Creator CREATOR =
            new Creator() {
                public User createFromParcel(Parcel in) {
                    return new User(in);
                }

                public User[] newArray(int size) {
                    return new User[size];
                }
            };
}
