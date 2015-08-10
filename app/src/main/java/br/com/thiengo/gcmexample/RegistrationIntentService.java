package br.com.thiengo.gcmexample;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;
import br.com.thiengo.gcmexample.network.NetworkConnection;




public class RegistrationIntentService extends IntentService {
    public static final String LOG = "LOG";


    public RegistrationIntentService(){
        super(LOG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean status = preferences.getBoolean("status", false);

        synchronized (LOG){
            InstanceID instanceID = InstanceID.getInstance( this );
            try {

                if( !status ){
                    String token = instanceID.getToken("794914603392",
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                            null);

                    Log.i(LOG, "TOKEN: "+token);

                    preferences.edit().putBoolean("status", token != null && token.trim().length() > 0 ).apply();

                    sendRegistrationId(token);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void sendRegistrationId( String token ){
        User user = new User();
        user.setRegistrationId( token );

        NetworkConnection
                .getInstance(this)
                .execute( new WrapObjToNetwork(user, "save-user"), RegistrationIntentService.class.getName() );
    }
}
