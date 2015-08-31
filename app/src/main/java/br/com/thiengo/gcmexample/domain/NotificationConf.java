package br.com.thiengo.gcmexample.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by viniciusthiengo on 8/30/15.
 */
public class NotificationConf implements Parcelable {

    public static final String STATUS_KEY = "br.com.thiengo.gcmexample.domain.NotificationConf.STATUS_KEY";
    public static final String METHOD_UPDATE = "update-notification-conf";
    public static final String[] CONF_LABELS = new String[]{"Normal",
            "Silenciar por uma hora",
            "Silenciar por um dia",
            "Silenciar por uma semana"};


    private int status;
    private long time;


    public NotificationConf() {}
    public NotificationConf(int status, long time) {
        this.status = status;
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void generateTimeByStatus(){
        if( status == 1 ){
            time += (60 * 60 * 1000); // ONE HOUR
        }
        else if( status == 2 ){
            time += (24 * 60 * 60 * 1000); // ONE DAY
        }
        else if( status == 3 ){
            time += (7 * 24 * 60 * 60 * 1000); // ONE WEEK
        }
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeLong(this.time);
    }

    protected NotificationConf(Parcel in) {
        this.status = in.readInt();
        this.time = in.readLong();
    }

    public static final Parcelable.Creator<NotificationConf> CREATOR = new Parcelable.Creator<NotificationConf>() {
        public NotificationConf createFromParcel(Parcel source) {
            return new NotificationConf(source);
        }

        public NotificationConf[] newArray(int size) {
            return new NotificationConf[size];
        }
    };
}
