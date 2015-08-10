package br.com.thiengo.gcmexample.domain;

/**
 * Created by viniciusthiengo on 8/10/15.
 */
public class User {
    private String registrationId;

    public User() {}
    public User(String registrationId) {
        this.registrationId = registrationId;
    }


    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }
}
