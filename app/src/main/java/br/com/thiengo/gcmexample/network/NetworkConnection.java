package br.com.thiengo.gcmexample.network;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

import br.com.thiengo.gcmexample.PM_UsersActivity;
import br.com.thiengo.gcmexample.domain.PushMessage;
import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;
import de.greenrobot.event.EventBus;

/**
 * Created by viniciusthiengo on 7/26/15.
 */
public class NetworkConnection {
    public static final String TAG = "LOG";

    private static NetworkConnection instance;
    private Context mContext;
    private RequestQueue mRequestQueue;
    private Transaction mTransaction;


    public NetworkConnection(Context c){
        mContext = c;
        mRequestQueue = getRequestQueue();
    }


    public static NetworkConnection getInstance( Context c ){
        if( instance == null ){
            instance = new NetworkConnection( c.getApplicationContext() );
        }
        return( instance );
    }


    public RequestQueue getRequestQueue(){
        if( mRequestQueue == null ){
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return(mRequestQueue);
    }


    public <T> void addRequestQueue( Request<T> request ){
        getRequestQueue().add(request);
    }


    public void execute( Transaction t, String tag ){
        mTransaction = t;
        WrapObjToNetwork obj = t.doBefore();
        execute(obj, tag);
    }


    public void execute( final WrapObjToNetwork obj, String tag ){
        Gson gson = new Gson();

        if( obj == null ){
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("jsonObject", gson.toJson(obj));
        Log.i("LOG", "params: " + gson.toJson(obj).toString());

        CustomRequest request = new CustomRequest(Request.Method.POST,
                "http://192.168.25.221:8888/PushMessageSerie/package/ctrl/CtrlUser.php",
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse(): " + response);

                        if( mTransaction != null ){
                            mTransaction.doAfter( response );
                        }
                        else{
                            // WHEN USER CONNECTS IN FIRST TIME
                                long id;
                                try {
                                    id = response.getLong("id");

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    id = 0;
                                }

                                Log.i(TAG, "NetworkConnection:ID = "+id);

                                Bundle bundle = new Bundle();
                                bundle.putLong(User.ID_KEY, id);

                                PushMessage pushMessage = new PushMessage();
                                pushMessage.setBundle(bundle);
                                pushMessage.setListenerLabel( PM_UsersActivity.class.getName() );
                                pushMessage.setCode( PM_UsersActivity.USER_DATA_CODE );

                                EventBus.getDefault().post( pushMessage );
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse(): "+error.getMessage());
                        if( mTransaction != null ){
                            mTransaction.doAfter( null );
                        }
                    }
                });

        request.setTag(tag);
        request.setRetryPolicy(new DefaultRetryPolicy(5000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addRequestQueue(request);
    }
}
