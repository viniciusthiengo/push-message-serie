package br.com.thiengo.gcmexample.extra;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

/**
 * Created by viniciusthiengo on 8/17/15.
 */
public class Util {
    public static boolean isMyApplicationTaskOnTop(Context context) {
        String packageName = context.getPackageName();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        if(recentTasks != null && recentTasks.size() > 0) {
            ActivityManager.RunningTaskInfo t = recentTasks.get(0);
            String pack = t.baseActivity.getPackageName();
            if(pack.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    public static boolean verifyConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }



    // TIME AGO
        private static final int SECOND_MILLIS = 1000;
        private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

        public static String getTimeAgo(long time) {
            if (time < 1000000000000L) {
                // if timestamp given in seconds, convert to millis
                time *= 1000;
            }

            long now = System.currentTimeMillis();
            if (time <= 0) {
                return null;
            }
            else if( time > now ){
                time = now;
            }

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "agora";
            }
            else if (diff < 2 * MINUTE_MILLIS) {
                return "a um minuto";
            }
            else if (diff < 50 * MINUTE_MILLIS) {
                return "a "+(diff / MINUTE_MILLIS) + " minutos";
            }
            else if (diff < 90 * MINUTE_MILLIS) {
                return "a uma hora";
            }
            else if (diff < 24 * HOUR_MILLIS) {
                return "a "+(diff / HOUR_MILLIS) + " horas";
            }
            else if (diff < 48 * HOUR_MILLIS) {
                return "ontem";
            }
            else {
                return "a "+(diff / DAY_MILLIS) + " dias";
            }
        }
}
