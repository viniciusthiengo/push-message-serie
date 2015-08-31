package br.com.thiengo.gcmexample.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by viniciusthiengo on 8/10/15.
 */
public class User implements Parcelable {

    public static final String METHOD_SAVE_USER = "save-user";

    public static final String USER_KEY = "br.com.thiengo.gcmexample.domain.User.USER_KEY";
    public static final String USER_TO_KEY = "br.com.thiengo.gcmexample.domain.User.USER_TO_KEY";
    public static final String ID_KEY = "br.com.thiengo.gcmexample.domain.User.ID_KEY";
    public static final String FIRST_TIME_KEY = "br.com.thiengo.gcmexample.domain.User.FIRST_TIME_KEY";


    private String registrationId;
    private long id;
    private String nickname;
    private int numberNewMessages;
    private boolean isTyping;
    private NotificationConf notificationConf;


    public User() {}
    public User(String registrationId) {
        this.registrationId = registrationId;
    }
    public User(long i, String n, int nnm, boolean it) {
        id = i;
        nickname = n;
        numberNewMessages = nnm;
        isTyping = it;
    }


    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setIsTyping(boolean isTyping) {
        this.isTyping = isTyping;
    }

    public int getNumberNewMessages() {
        return numberNewMessages;
    }

    public void setNumberNewMessages(int numberNewMessages) {
        this.numberNewMessages = numberNewMessages;
    }

    public NotificationConf getNotificationConf() {
        return notificationConf;
    }

    public void setNotificationConf(NotificationConf notificationConf) {
        this.notificationConf = notificationConf;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.registrationId);
        dest.writeLong(this.id);
        dest.writeString(this.nickname);
        dest.writeInt(this.numberNewMessages);
        dest.writeByte(isTyping ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.notificationConf, 0);
    }

    protected User(Parcel in) {
        this.registrationId = in.readString();
        this.id = in.readLong();
        this.nickname = in.readString();
        this.numberNewMessages = in.readInt();
        this.isTyping = in.readByte() != 0;
        this.notificationConf = in.readParcelable(NotificationConf.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
