package br.com.thiengo.gcmexample.domain;

/**
 * Created by viniciusthiengo on 8/10/15.
 */
public class PushMessage {
    private String title;
    private String message;


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
}
