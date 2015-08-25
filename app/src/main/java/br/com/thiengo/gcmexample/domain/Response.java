package br.com.thiengo.gcmexample.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by viniciusthiengo on 8/2/15.
 */
public class Response implements Parcelable {
    private int code;
    private boolean status;
    private String message;


    public Response() {}
    public Response(boolean status, String message) {
        this.status = status;
        this.message = message;
    }
    public Response(int code, boolean status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.code);
        dest.writeByte(status ? (byte) 1 : (byte) 0);
        dest.writeString(this.message);
    }

    protected Response(Parcel in) {
        this.code = in.readInt();
        this.status = in.readByte() != 0;
        this.message = in.readString();
    }

    public static final Parcelable.Creator<Response> CREATOR = new Parcelable.Creator<Response>() {
        public Response createFromParcel(Parcel source) {
            return new Response(source);
        }

        public Response[] newArray(int size) {
            return new Response[size];
        }
    };
}
