package br.com.thiengo.gcmexample;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import br.com.thiengo.gcmexample.conf.Configuration;
import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;
import br.com.thiengo.gcmexample.extra.Pref;
import br.com.thiengo.gcmexample.network.NetworkConnection;




public class RegistrationIntentService extends IntentService {
    public static final String LOG = "LOG";


    public RegistrationIntentService(){
        super(LOG);
    }

    @Override
    protected void onHandleIntent( Intent intent) {

        int id = Integer.parseInt( Pref.retrievePrefKeyValue(getApplicationContext(),
                Pref.PREF_KEY_ID,
                "0") );

        String nickname = Pref.retrievePrefKeyValue(getApplicationContext(),
                Pref.PREF_KEY_NICKNAME);


        synchronized (LOG){
            InstanceID instanceID = InstanceID.getInstance( this );
            try {

                if( id == 0 ){
                    String token = instanceID.getToken(Configuration.SENDER_ID,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                            null);

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
                .execute( new WrapObjToNetwork( user, User.METHOD_SAVE_USER ),
                        RegistrationIntentService.class.getName() );
    }
}
