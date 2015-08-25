package br.com.thiengo.gcmexample;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import br.com.thiengo.gcmexample.conf.Configuration;
import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;
import br.com.thiengo.gcmexample.network.NetworkConnection;




public class RegistrationIntentService extends IntentService {
    public static final String LOG = "LOG";


    public RegistrationIntentService(){
        super(LOG);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
        boolean status = preferences.getBoolean("status", false);
        String nickname = preferences.getString(PM_LoginActivity.PREF_KEY_NICKNAME, "");


        synchronized (LOG){
            InstanceID instanceID = InstanceID.getInstance( this );
            try {

                if( !status ){
                    String token = instanceID.getToken(Configuration.SENDER_ID,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                            null);

                    preferences.edit().putBoolean("status", token != null && token.trim().length() > 0 ).apply();
                    sendRegistrationId(token, nickname);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void sendRegistrationId( String token, String nickname ){
        User user = new User();
        user.setRegistrationId( token );
        user.setNickname( nickname );

        NetworkConnection
                .getInstance(this)
                .execute( new WrapObjToNetwork(user, "save-user"), RegistrationIntentService.class.getName() );
    }
}
