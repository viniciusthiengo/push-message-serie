package br.com.thiengo.gcmexample.domain;

import android.os.Bundle;

/**
 * Created by viniciusthiengo on 8/10/15.
 */
public class PushMessage {
    private String title;
    private String message;
    private Bundle bundle;
    private String listenerLabel;
    private int code;


    public PushMessage() {}
    public PushMessage(String title, String message) {
        this.title = title;
        this.message = message;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public String getListenerLabel() {
        return listenerLabel;
    }

    public void setListenerLabel(String listenerLabel) {
        this.listenerLabel = listenerLabel;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
