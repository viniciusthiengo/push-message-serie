package br.com.thiengo.gcmexample.domain;

import java.util.LinkedList;

/**
 * Created by viniciusthiengo on 7/26/15.
 */
public class WrapObjToNetwork {
    private User user;
    private User userFrom;
    private Message message;
    private LinkedList<Message> messages;
    private String method;


    public WrapObjToNetwork( LinkedList<Message> messages, String method) {
        this.messages = messages;
        this.method = method;
    }
    public WrapObjToNetwork(User user, User userFrom, String method) {
        this.user = user;
        this.userFrom = userFrom;
        this.method = method;
    }
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

    public User getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(User userFrom) {
        this.userFrom = userFrom;
    }

    public LinkedList<Message> getMessages() {
        return messages;
    }

    public void setMessages(LinkedList<Message> messages) {
        this.messages = messages;
    }
}
