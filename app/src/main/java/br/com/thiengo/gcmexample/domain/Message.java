package br.com.thiengo.gcmexample.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by viniciusthiengo on 8/23/15.
 */
public class Message implements Parcelable {
    public static final String METHOD_SAVE = "save-message";
    public static final String METHOD_REMOVE = "remove-message";
    public static final String METHOD_LOAD_OLD = "load-old-messages";
    public static final String METHOD_GET = "get-messages";


    public static final String MESSAGE_KEY = "br.com.thiengo.gcmexample.domain.Message.MESSAGE_KEY";
    public static final String MESSAGES_SUMMARY_KEY = "br.com.thiengo.gcmexample.domain.Message.MESSAGES_SUMMARY_KEY";

    private long id;
    private User userFrom;
    private User userTo;
    private String message;
    private long regTime;
    private int wasRead;
    private String ackId;


    public Message() {}
    public Message(User userFrom, User userTo, String message, long regTime) {
        this.userFrom = userFrom;
        this.userTo = userTo;
        this.message = message;
        this.regTime = regTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(User userFrom) {
        this.userFrom = userFrom;
    }

    public User getUserTo() {
        return userTo;
    }

    public void setUserTo(User userTo) {
        this.userTo = userTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getWasRead() {
        return wasRead;
    }

    public void setWasRead(int wasRead) {
        this.wasRead = wasRead;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }

    public String getAckId() {
        return ackId;
    }

    public void setAckId(String ackId) {
        this.ackId = ackId;
    }

    public String getRegTimeForHuman(){
        String aux = "";
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( regTime );

        aux += c.get(Calendar.DAY_OF_MONTH) < 10 ? "0"+c.get(Calendar.DAY_OF_MONTH) : c.get(Calendar.DAY_OF_MONTH);
        aux += "/";
        aux += c.get(Calendar.MONTH) < 10 ? "0"+c.get(Calendar.MONTH) : c.get(Calendar.MONTH);
        aux += "/";
        aux += c.get(Calendar.YEAR);
        aux += " às ";
        aux += c.get(Calendar.HOUR_OF_DAY) < 10 ? "0"+c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY);
        aux += "h";
        aux += c.get(Calendar.MINUTE) < 10 ? "0"+c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE);

        return( aux );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeParcelable(this.userFrom, 0);
        dest.writeParcelable(this.userTo, 0);
        dest.writeString(this.message);
        dest.writeLong(this.regTime);
        dest.writeInt(this.wasRead);
        dest.writeString(this.ackId);
    }

    protected Message(Parcel in) {
        this.id = in.readLong();
        this.userFrom = in.readParcelable(User.class.getClassLoader());
        this.userTo = in.readParcelable(User.class.getClassLoader());
        this.message = in.readString();
        this.regTime = in.readLong();
        this.wasRead = in.readInt();
        this.ackId = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
