package br.com.thiengo.gcmexample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import br.com.thiengo.gcmexample.domain.Message;
import br.com.thiengo.gcmexample.domain.NotificationConf;
import br.com.thiengo.gcmexample.domain.PushMessage;
import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.extra.Pref;
import br.com.thiengo.gcmexample.extra.Util;
import br.com.thiengo.gcmexample.receiver.NotificationReceiver;
import de.greenrobot.event.EventBus;





public class MyGcmListenerService extends GcmListenerService  {
    public static final String TAG = "LOG";


    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "--> "+data);

        // TYPE NOTIFICATION
            if( data != null
                && data.getString("type") != null ){

                if( Integer.parseInt(data.getString("type")) == 1 ){

                    // MANY MESSAGES
                        try{
                            Gson gson = new Gson();

                            JSONObject jsonObject = new JSONObject( data.getString("userTo_new_messages") );
                            JSONArray jsonArray = jsonObject.getJSONArray("messages");
                            ArrayList<Message> messages = new ArrayList<>();

                            for( int i = 0, tamI = jsonArray.length(); i < tamI; i++ ){
                                Message m = gson.fromJson( jsonArray.getJSONObject( i ).toString(), Message.class );
                                messages.add(m);
                            }

                            data.putParcelableArrayList(Message.MESSAGES_SUMMARY_KEY, messages);
                        }
                        catch( Exception e){
                            e.printStackTrace();
                        }

                    // MAIN MESSAGE
                        Message m = new Message();
                        m.setId(Long.parseLong(data.getString("id")));
                        m.setMessage(data.getString("message"));
                        m.setRegTime(Long.parseLong(data.getString("regTime")));

                        m.setUserFrom(new User());
                        m.getUserFrom().setId(Long.parseLong(data.getString("userFrom_id")));
                        m.getUserFrom().setNickname(data.getString("userFrom_nickname"));

                        m.setUserTo(new User());
                        m.getUserTo().setId(Long.parseLong(data.getString("userTo_id")));
                        m.getUserTo().setNickname(data.getString("userTo_nickname"));
                        m.getUserTo().setNotificationConf(new NotificationConf());
                        m.getUserTo().getNotificationConf().setStatus( Integer.parseInt(data.getString("userTo_notification_status")) );
                        m.getUserTo().getNotificationConf().setTime( Long.parseLong(data.getString("userTo_notification_time")) );

                        data.putParcelable(Message.MESSAGE_KEY, m);
                }
                else if( Integer.parseInt(data.getString("type")) == 2
                        || Integer.parseInt(data.getString("type")) == 3 ){

                    Message m = new Message();
                    m.setId(Long.parseLong(data.getString("id")));

                    m.setUserFrom(new User());
                    m.getUserFrom().setId(Long.parseLong(data.getString("userFrom_id")));

                    m.setUserTo(new User());
                    m.getUserTo().setId(Long.parseLong(data.getString("userTo_id")));

                    data.putParcelable(Message.MESSAGE_KEY, m);
                }
            }


        if( !Util.isMyApplicationTaskOnTop(this)
                && data != null
                && data.getString("type") != null
                && Integer.parseInt(data.getString("type")) == 1 ){

            long userId = Long.parseLong( Pref.retrievePrefKeyValue( getApplicationContext(), Pref.PREF_KEY_ID, "0" ) );
            Message m = data.getParcelable(Message.MESSAGE_KEY);

            // VERIFY THAT IT'S NOT THE OWNER OF THE MESSAGE
            if( m != null
                && m.getUserFrom() != null
                && m.getUserFrom().getId() != userId ){

                setNotificationApp( data );
            }
        }
        else{
            if( PM_MessagesActivity.IS_ON_TOP
                    || PM_UsersActivity.IS_ON_TOP ){

                PushMessage pushMessage = new PushMessage();

                if( PM_MessagesActivity.IS_ON_TOP ){
                    pushMessage.setListenerLabel(PM_MessagesActivity.class.getName());
                    pushMessage.setBundle(data);

                    if( Integer.parseInt(data.getString("type")) == 1 ){
                        pushMessage.setCode( PM_MessagesActivity.NEW_MESSAGE_CODE );
                    }
                    else if( Integer.parseInt(data.getString("type")) == 2 ){
                        pushMessage.setCode( PM_MessagesActivity.MESSAGE_WAS_READ_CODE );
                    }
                    else if( Integer.parseInt(data.getString("type")) == 3 ){
                        pushMessage.setCode( PM_MessagesActivity.MESSAGE_REMOVED_CODE );
                    }
                }
                else if( PM_UsersActivity.IS_ON_TOP ){
                    pushMessage.setListenerLabel( PM_UsersActivity.class.getName() );
                    pushMessage.setCode(PM_MessagesActivity.NEW_MESSAGE_CODE);
                    pushMessage.setBundle(data);
                }

                EventBus.getDefault().post( pushMessage );
            }
        }
    }


    private void setNotificationApp( final Bundle data ){

        String userNickname = Pref.retrievePrefKeyValue(getApplicationContext(), Pref.PREF_KEY_NICKNAME, "");
        ArrayList<Message> messages = data.getParcelableArrayList(Message.MESSAGES_SUMMARY_KEY);
        final int id = 6565;

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setTicker( data.getString("title") )
                .setSmallIcon( R.drawable.ic_new_message )
                .setContentTitle( data.getString("title") )
                .setAutoCancel(true);


        Intent intent = new Intent(this, PM_LoginActivity.class);
        intent.putExtras(data);

        PendingIntent pi = PendingIntent.getActivity( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        builder.setContentIntent(pi);


        if( messages != null
                && messages.size() == 1 ){
            // BIG CONTENT
                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                bigText.bigText(data.getString("userFrom_nickname") + ": " + data.getString("message"));
                builder.setStyle(bigText);
        }
        else if( messages != null ){

            // INPUT STYLE
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                inboxStyle.setSummaryText(userNickname);
                int number = Integer.parseInt( data.getString("userTo_amount_new_messages") );

                for( int i = 0, tamI = messages.size(); i < tamI; i++ ){

                    String messsage = messages.get( i ).getUserFrom().getNickname() + ": " + messages.get( i ).getMessage();
                    int size = messsage.indexOf(":");
                    Spannable sb = new SpannableString( messsage );

                    sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, size, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    inboxStyle.addLine(sb);
                }
                inboxStyle.setBigContentTitle( number + " novas mensagens" );
                builder.setNumber(number);
                builder.setStyle(inboxStyle);

            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            // WHEN TO MAKE SOME NOISE
            if( data.getParcelable(Message.MESSAGE_KEY) != null ){
                Message m = data.getParcelable(Message.MESSAGE_KEY);

                if( m.getUserTo() != null
                        && ( m.getUserTo().getNotificationConf().getStatus() == 0
                        || m.getUserTo().getNotificationConf().getTime() < System.currentTimeMillis() ) ){

                    Pref.savePrefKeyValue(getApplicationContext(),
                            Pref.PREF_KEY_NOTIFICATION_STATUS + "_" + m.getUserFrom(),
                            String.valueOf( 0 ));

                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    if( am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE ){
                        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

                        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    }
                    else if( am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL ){
                        Uri uri = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
                        builder.setSound(uri);

                        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    }
                }
            }
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, builder.build());
    }


    private void setNotificationApp_OLD( final Bundle data ){
        final int id = 6565;

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setTicker( data.getString("body") )
                .setSmallIcon( R.drawable.ic_new_message )
                .setContentTitle( data.getString("title") )
                .setContentText( data.getString("body") )
                .setAutoCancel( true );

        /*TaskStackBuilder stack = TaskStackBuilder.create(this);
        stack.addParentStack( SecondActivity.class );*/

        Intent it = new Intent(this, ThirdActivity.class);
        Bundle b = new Bundle();
        b.putString("data", "Test");
        it.putExtras(b);
        it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        //stack.addNextIntent( it );

        PendingIntent pi = PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent pi = stack.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
        builder.setContentIntent(pi);


        // BIG CONTENT
            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText( data.getString("body") );
            builder.setStyle(bigText);

            /*NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                String[] messages = new String[6];
                builder.setNumber(messages.length);
            inboxStyle.setSummaryText("thiengocalopsita@gmail.com");
            for( int i = 0; i < messages.length; i++ ){
                messages[i] = "Test "+(new Random()).nextInt(9999);
                inboxStyle.addLine( messages[i] );
            }
            builder.setStyle(inboxStyle);*/


        // ACTION BUTTON
            //RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
            // YES
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.setAction(NotificationReceiver.YES_NOTIFICATION_BUTTON);
            Bundle bu = new Bundle();
            bu.putInt("data", 1);
            PendingIntent pb = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //remoteViews.setOnClickPendingIntent( R.id.bt_yes, pb );
            builder.addAction(R.drawable.ic_yes, "Yes", pb);

            // MAYBE
            intent = new Intent(this, NotificationReceiver.class);
            intent.setAction(NotificationReceiver.MAYBE_NOTIFICATION_BUTTON);
            bu = new Bundle();
            bu.putInt("data", 2);
            pb = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //remoteViews.setOnClickPendingIntent( R.id.bt_maybe, pb );
            builder.addAction( R.drawable.ic_maybe, "Maybe", pb );

            // NO
            intent = new Intent(this, NotificationReceiver.class);
            intent.setAction(NotificationReceiver.NO_NOTIFICATION_BUTTON);
            bu = new Bundle();
            bu.putInt("data", 3);
            pb = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //remoteViews.setOnClickPendingIntent(R.id.bt_no, pb);
            builder.addAction(R.drawable.ic_no, "No", pb);

            //builder.setContent( remoteViews );

        final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, builder.build());

        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap img1 = null, img2 = null;

                try {
                    img1 = Picasso.with(getBaseContext()).load( data.getString("large_icon") ).get();
                    img2 = Picasso.with(getBaseContext()).load( data.getString("big_picture") ).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                builder.setLargeIcon( img1 );
                //builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(img2));


                /*for( int i = 0; i < 5; i++ ){
                    builder.setProgress( 0, 0, true );
                    builder.setContentTitle("Download");
                    builder.setContentText("download music...");

                    nm.notify(id, builder.build());

                    SystemClock.sleep(1000);
                }

                builder.setProgress( 0, 0, false );
                builder.setContentTitle("Download");
                builder.setContentText("Finalizado");

                nm.notify( id, builder.build() );*/

                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

                Uri uri = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
                builder.setSound(uri);

                nm.notify(id, builder.build());
            }
        }.start();

        /*builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        Uri uri = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
        builder.setSound(uri);

        nm.notify( id, builder.build() );
        */
    }



}
