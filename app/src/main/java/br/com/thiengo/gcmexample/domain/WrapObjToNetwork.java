package br.com.thiengo.gcmexample.domain;

/**
 * Created by viniciusthiengo on 7/26/15.
 */
public class WrapObjToNetwork {
    private User user;
    private Message message;
    private String method;


    public WrapObjToNetwork(User user, String method) {
        this.user = user;
        this.method = method;
    }
    public WrapObjToNetwork(Message message, String method) {
        this.message = message;
        this.method = method;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
