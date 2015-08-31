package br.com.thiengo.gcmexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.com.thiengo.gcmexample.adapter.UserAdapter;
import br.com.thiengo.gcmexample.domain.Message;
import br.com.thiengo.gcmexample.domain.NotificationConf;
import br.com.thiengo.gcmexample.domain.PushMessage;
import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;
import br.com.thiengo.gcmexample.extra.Pref;
import br.com.thiengo.gcmexample.extra.Util;
import br.com.thiengo.gcmexample.network.NetworkConnection;
import br.com.thiengo.gcmexample.network.Transaction;
import de.greenrobot.event.EventBus;

public class PM_UsersActivity extends AppCompatActivity implements Transaction {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String TAG = "LOG";
    public static final String LIST_KEY = "br.com.thiengo.gcmexample.PM_UsersActivity.LIST_KEY";
    public static final int USER_DATA_CODE = 6892; // ANY INT
    public static final int USER_NOTIFICATION_CONF_UPDATED_CODE = 6893; // ANY INT
    public static boolean IS_ON_TOP;

    private RecyclerView mRecyclerView;
    private ArrayList<User> mList;
    private CoordinatorLayout clContainer;
    private ProgressBar mPbLoad;
    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm__users);

        // GET USER FROM INTENT (THAT CAME FROM LOGIN ACTIVITY)
            int isFirstTime = 0;
            if( getIntent() != null
                    && getIntent().getExtras() != null
                    && getIntent().getExtras().getParcelable(User.USER_KEY) != null ){

                mUser = getIntent().getExtras().getParcelable(User.USER_KEY);
                isFirstTime = getIntent().getExtras().getInt(User.FIRST_TIME_KEY, 0);
            }
            else if( getIntent() != null
                    && getIntent().getExtras() != null
                    && getIntent().getExtras().getParcelable(Message.MESSAGE_KEY) != null ){

                Message m = getIntent().getExtras().getParcelable(Message.MESSAGE_KEY);

                long id = Long.parseLong( Pref.retrievePrefKeyValue(getApplicationContext(),
                        Pref.PREF_KEY_ID,
                        "0") ) ;

                mUser = m.getUserFrom().getId() == id ? m.getUserFrom() : m.getUserTo();
                User mUserTo = m.getUserFrom().getId() == id ? m.getUserTo() : m.getUserFrom();

                Bundle bundle = new Bundle();
                bundle.putParcelable(User.USER_KEY, mUser);
                bundle.putParcelable(User.USER_TO_KEY, mUserTo );

                ArrayList<Message> messages = getIntent().getExtras().getParcelableArrayList(Message.MESSAGES_SUMMARY_KEY);

                // CALL MESSAGE ACTIVITY ONLY IF IT HAS JUST ONE MESSAGE
                if( messages == null
                        || messages.size() == 1 ){

                    Intent intent = new Intent(this, PM_MessagesActivity.class);
                    intent.putExtras(bundle);
                    startActivity( intent );
                }
            }
            else{

                Pref.savePrefKeyValue(getApplicationContext(), Pref.PREF_KEY_NICKNAME, "");
                Pref.savePrefKeyValue(getApplicationContext(), Pref.PREF_KEY_ID, "0");

                Intent intent = new Intent( this, PM_LoginActivity.class );
                startActivity(intent);
                finish();
            }


        clContainer = (CoordinatorLayout) findViewById(R.id.cl_container);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.tb_main);
        mToolbar.setTitle("APP Push Message");
        setSupportActionBar(mToolbar);

        mPbLoad = (ProgressBar) findViewById(R.id.pb_load);
        mPbLoad.setVisibility( isFirstTime == 1 ? View.VISIBLE : View.GONE );

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setVisibility( isFirstTime == 1 ? View.GONE : View.VISIBLE );


        // SAVEDINSTANCESTATE VERIFICATION
            if(savedInstanceState != null){
                mList = savedInstanceState.getParcelableArrayList(LIST_KEY);
            }
            else{
                mList = new ArrayList<>();

                if( isFirstTime == 0 ){
                    // CONNECTION
                        NetworkConnection.getInstance(this).execute(this, PM_UsersActivity.class.getName());
                }
            }


        // EVENT BUS
            EventBus.getDefault().register(this);


        // VERIFY AND GENERATE REGID
            if( checkPlayServices() ){
                Intent it = new Intent(this, RegistrationIntentService.class);

                startService(it);
            }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // RECYCLER VIEW CONF
            mRecyclerView.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager( this );
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(llm);

            UserAdapter mAdapter = new UserAdapter(this, mList, mUser);
            mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IS_ON_TOP = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        IS_ON_TOP = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(LIST_KEY, mList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();

        // STOP CONNECTION
        NetworkConnection.getInstance(this).getRequestQueue().cancelAll(PM_UsersActivity.class.getName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode == PM_NicknameActivity.CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getExtras() != null
                && data.getExtras().getParcelable(User.USER_KEY) != null ){

            // CHANGE NICKNAME IF IT EXISTS
                User aux = data.getExtras().getParcelable(User.USER_KEY);
                if( aux.getNickname() != null
                        && !aux.getNickname().trim().isEmpty() ){

                    mUser.setNickname( aux.getNickname().trim() );
                }
        }
    }


    // UTIL
        private int getItemListPosition(ArrayList<User> l, User u){
            for( int i = 0, tamI = l.size(); i < tamI; i++ ){
                if( l.get(i).getId() == u.getId() ){
                    return( i );
                }
            }
            return( -1 );
        }

        private boolean checkPlayServices() {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Log.i("LOG", "This device is not supported.");
                    finish();
                }
                return false;
            }
            return true;
        }

        public void sendConfigNotification( User userFrom ){

            int statusNotification = Integer.parseInt( Pref.retrievePrefKeyValue(getApplicationContext(),
                    Pref.PREF_KEY_NOTIFICATION_STATUS + "_" + userFrom.getId(),
                    "0") );

            if( statusNotification != userFrom.getNotificationConf().getStatus() ){

                // SP - UPDATE NOTIFICATION DATA
                    Pref.savePrefKeyValue(getApplicationContext(),
                        Pref.PREF_KEY_NOTIFICATION_STATUS + "_" + userFrom.getId(),
                        String.valueOf( userFrom.getNotificationConf().getStatus() ));

                    Pref.savePrefKeyValue(getApplicationContext(),
                        Pref.PREF_KEY_NOTIFICATION_STATUS_OLD + "_" + userFrom.getId(),
                        String.valueOf( statusNotification ));

                NetworkConnection
                        .getInstance(getApplicationContext())
                        .execute(
                                new WrapObjToNetwork(mUser, userFrom, NotificationConf.METHOD_UPDATE),
                                PM_UsersActivity.class.getName());
            }
        }


    // MENU
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_pm__users, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.action_configurations) {

                Bundle bundle = new Bundle();
                bundle.putParcelable( User.USER_KEY, mUser );

                Intent intent = new Intent( this, PM_NicknameActivity.class );
                intent.putExtras( bundle );

                startActivityForResult(intent, PM_NicknameActivity.CODE);
                return true;
            }
            else if( id == R.id.action_logout ){
                Pref.savePrefKeyValue(getApplicationContext(), Pref.PREF_KEY_ID, "0");

                Intent intent = new Intent( this, PM_LoginActivity.class );
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            }

            return super.onOptionsItemSelected(item);
        }


    // LISTENERS
        public void onEvent( final PushMessage pushMessage ){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if( pushMessage.getListenerLabel().equalsIgnoreCase( PM_UsersActivity.class.getName() ) ){

                        if( pushMessage.getCode() == USER_DATA_CODE ){

                            if( pushMessage.getBundle() != null
                                    && pushMessage.getBundle().getLong(User.ID_KEY) > 0 ){

                                mUser.setId(pushMessage.getBundle().getLong(User.ID_KEY));

                                // SAVING ID ON PREFERENCES
                                    Pref.savePrefKeyValue(
                                        getApplicationContext(),
                                        Pref.PREF_KEY_ID,
                                        String.valueOf(mUser.getId()) );

                                NetworkConnection.getInstance( PM_UsersActivity.this )
                                        .execute(PM_UsersActivity.this,
                                                PM_UsersActivity.class.getName());
                            }
                            else{
                                // CAME BACK TO LOGIN ACTIVITY
                                Intent intent = new Intent( PM_UsersActivity.this, PM_LoginActivity.class );
                                startActivity(intent);
                                finish();
                            }
                        }
                        else if( pushMessage.getCode() == PM_MessagesActivity.NEW_MESSAGE_CODE ){

                            if( mList.size() == 0 ){
                                NetworkConnection.getInstance(PM_UsersActivity.this).execute(PM_UsersActivity.this,
                                        PM_UsersActivity.class.getName());
                                return;
                            }

                            Message m = pushMessage.getBundle().getParcelable(Message.MESSAGE_KEY);

                            UserAdapter adapter = (UserAdapter) mRecyclerView.getAdapter();
                            int position = getItemListPosition( mList, m.getUserFrom() );

                            if( position >= 0 ){ // UPDATE
                                mList.get( position ).setNickname( m.getUserFrom().getNickname() );
                                mList.get( position ).setNumberNewMessages( mList.get(position).getNumberNewMessages() + 1 );

                                adapter.notifyItemChanged( position );
                            }
                            else if( m.getUserFrom().getId() != mUser.getId() ){ // INSERT
                                m.getUserFrom().setNumberNewMessages(1);
                                adapter.addListItem(m.getUserFrom(), 0 );
                            }
                        }
                        else if( pushMessage.getCode() == PM_UsersActivity.USER_NOTIFICATION_CONF_UPDATED_CODE ){

                            User userFrom = pushMessage.getBundle().getParcelable(User.USER_KEY);
                            User u = mList.get(getItemListPosition(mList, userFrom));

                            if( userFrom != null
                                    && userFrom.getNotificationConf().getStatus() != u.getNotificationConf().getStatus() ){

                                u.getNotificationConf().setStatus( userFrom.getNotificationConf().getStatus() );

                                Pref.savePrefKeyValue(getApplicationContext(),
                                        Pref.PREF_KEY_NOTIFICATION_STATUS + "_" + u.getId(),
                                        String.valueOf( userFrom.getNotificationConf().getStatus() ));

                                Snackbar.make( clContainer,
                                        "Atualização notificações de \""+u.getNickname()+"\" falhou, tente novamente",
                                        Snackbar.LENGTH_LONG ).show();
                            }
                        }
                    }
                }
            });
        }


    // NETWORK
        @Override
        public WrapObjToNetwork doBefore() {
            mPbLoad.setVisibility(View.VISIBLE);

            if( Util.verifyConnection(this) ){
                return( new WrapObjToNetwork( mUser, "get-users" ) );
            }
            return null;
        }

        @Override
        public void doAfter(JSONObject jsonObject) {
            mPbLoad.setVisibility(View.GONE );

            if( jsonObject != null ){
                UserAdapter adapter = (UserAdapter) mRecyclerView.getAdapter();
                Gson gson = new Gson();

                try{
                    JSONArray jsonArray = jsonObject.getJSONArray("users");

                    for(int i = 0, tamI = jsonArray.length(); i < tamI; i++){

                        User u = gson.fromJson( jsonArray.getJSONObject( i ).toString(), User.class );
                        int position = getItemListPosition( mList, u);

                        if( position < 0 ){
                            adapter.addListItem( u, mList.size() );
                        }
                        else{
                            mList.get( position ).setNickname(u.getNickname());
                            mList.get( position ).setNumberNewMessages(u.getNumberNewMessages());

                            adapter.notifyItemChanged( position );
                        }
                    }
                }
                catch(JSONException e){
                    Log.i(TAG, "doAfter(): "+e.getMessage());
                }
            }
            else{
                Toast.makeText(this, "Falhou. Tente novamente.", Toast.LENGTH_SHORT).show();
            }


            // WHEN NO USERS ARE CONNECTED
                mRecyclerView.setVisibility(mList.isEmpty() ? View.GONE : View.VISIBLE);
                if( mList.isEmpty() ){
                    TextView tv = new TextView( this );
                    tv.setText( "Nenhum usuário connectado a você." );
                    tv.setTextColor( getResources().getColor( R.color.colorPrimarytext ) );
                    tv.setId( 1 );
                    tv.setLayoutParams( new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT )  );
                    tv.setGravity(Gravity.CENTER);

                    clContainer.addView( tv );
                }
                else if( clContainer.findViewById(1) != null ) {
                    clContainer.removeView( clContainer.findViewById(1) );
                }
        }
}
