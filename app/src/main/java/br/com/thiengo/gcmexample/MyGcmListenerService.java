package br.com.thiengo.gcmexample;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import br.com.thiengo.gcmexample.domain.PushMessage;
import de.greenrobot.event.EventBus;





public class MyGcmListenerService extends GcmListenerService  {
    public static final String LOG = "LOG";


    @Override
    public void onMessageReceived(String from, Bundle data) {
        //super.onMessageReceived(from, data);
        String title = data.getString("title");
        String message = data.getString("message");



        EventBus.getDefault().post( new PushMessage( title, message ) );
    }
}
