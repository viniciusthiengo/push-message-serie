package br.com.thiengo.gcmexample;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

import br.com.thiengo.gcmexample.adapter.MessageAdapter;
import br.com.thiengo.gcmexample.domain.Message;
import br.com.thiengo.gcmexample.domain.PushMessage;
import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;
import br.com.thiengo.gcmexample.extra.Util;
import br.com.thiengo.gcmexample.network.NetworkConnection;
import br.com.thiengo.gcmexample.network.Transaction;
import de.greenrobot.event.EventBus;

public class PM_MessagesActivity extends AppCompatActivity implements Transaction, View.OnClickListener {
    public static final String TAG = "LOG";
    public static final String LIST_KEY = "br.com.thiengo.gcmexample.PM_MessagesActivity.LIST_KEY";
    //public static final String IS_FROM_NOTIFICATION_KEY = "br.com.thiengo.gcmexample.PM_MessagesActivity.IS_FROM_NOTIFICATION_KEY";
    public static final int NEW_MESSAGE_CODE = 9986; // ANY INT
    public static final int MESSAGE_WAS_READ_CODE = 9987; // ANY INT
    public static final int MESSAGE_REMOVED_CODE = 9988; // ANY INT
    public static boolean IS_ON_TOP;

    private RecyclerView mRecyclerView;
    private ArrayList<Message> mList;
    //private CoordinatorLayout clContainer;
    //private ProgressBar mPbLoad;
    private FrameLayout mFlPbLoad;
    private EditText etMessage;
    private ImageButton btSendMessage;
    private TextView tvEmptyList;

    private User mUserFrom;
    private User mUserTo;
    private String mMethod;
    protected boolean mIsLastItem;
    //private boolean isFromNotification;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm__messages);


        // GET USER FROM AND USER TO FROM INTENT
            if( getIntent() != null
                    && getIntent().getExtras() != null
                    && getIntent().getExtras().getParcelable(User.USER_KEY) != null
                    && getIntent().getExtras().getParcelable(User.USER_TO_KEY) != null){

                mUserFrom = getIntent().getExtras().getParcelable(User.USER_KEY);
                mUserTo = getIntent().getExtras().getParcelable(User.USER_TO_KEY);
            }
            else{
                finish();
            }

        //clContainer = (CoordinatorLayout) findViewById(R.id.cl_container);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.tb_main);
        mToolbar.setTitle(mUserTo.getNickname());
        setSupportActionBar(mToolbar);
        if( getSupportActionBar() != null ){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        tvEmptyList = (TextView) findViewById(R.id.tv_empty_list);
        mFlPbLoad = (FrameLayout) findViewById(R.id.fl_pb_load);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);

        etMessage = (EditText) findViewById(R.id.et_message);
        btSendMessage = (ImageButton) findViewById(R.id.bt_send_message);
        btSendMessage.setOnClickListener(this);


        // SAVEDINSTANCESTATE VERIFICATION
            if(savedInstanceState != null){
                mList = savedInstanceState.getParcelableArrayList(LIST_KEY);
                //isFromNotification = savedInstanceState.getBoolean(IS_FROM_NOTIFICATION_KEY, false);
            }
            else{
                mList = new ArrayList<>();

                // CONNECTION - GET MESSAGES
                    mMethod = Message.METHOD_GET;
                    NetworkConnection.getInstance(this).execute(this, PM_MessagesActivity.class.getName());
            }


        // EVENT BUS
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // RECYCLER VIEW CONF
            mRecyclerView.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager( this );
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            llm.setReverseLayout(true);
            mRecyclerView.setLayoutManager(llm);

            MessageAdapter mAdapter = new MessageAdapter(this, mList, mUserFrom);
            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();

                    if (!mIsLastItem
                            && mList.size() == llm.findLastCompletelyVisibleItemPosition() + 1) {

                        mMethod = Message.METHOD_LOAD_OLD;
                        NetworkConnection
                                .getInstance(PM_MessagesActivity.this)
                                .execute(PM_MessagesActivity.this, PM_MessagesActivity.class.getName());
                    }
                }
            });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(LIST_KEY, mList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IS_ON_TOP = true;
        runClock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        IS_ON_TOP = false;
    }

    @Override
    public void onStop() {
        super.onStop();

        // STOP CONNECTION
            NetworkConnection.getInstance(this).getRequestQueue().cancelAll(PM_MessagesActivity.class.getName());
    }


    // UTIL
        private int getItemListPosition(ArrayList<Message> l, Message m){
            for( int i = 0, tamI = l.size(); i < tamI; i++ ){
                if( l.get(i).getId() == m.getId() ){
                    return( i );
                }
            }
            return( -1 );
        }

        private void callUpdateMessageWasRead( LinkedList<Message> messages ){
            // ONLY NOT READ MESSAGE - SIMULATION ACK SCRIPT
            if( !messages.isEmpty() ){
                WrapObjToNetwork won = new WrapObjToNetwork( messages, "update-messages-read" );

                NetworkConnection
                        .getInstance(getApplicationContext())
                        .execute(won, PM_UsersActivity.class.getName());
            }
        }

        private void runClock(){
            new Thread(){
                @Override
                public void run() {
                    if( IS_ON_TOP ){
                        SystemClock.sleep(60000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if( mRecyclerView != null ){
                                    mRecyclerView.getAdapter().notifyDataSetChanged();
                                    runClock();
                                }
                            }
                        });
                    }
                }
            }.start();
        }

        public void removeMessage( Message m ){

            NetworkConnection
                    .getInstance(getApplicationContext())
                    .execute(
                            new WrapObjToNetwork(m, Message.METHOD_REMOVE),
                            PM_UsersActivity.class.getName());
        }


    // MENU
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_pm__messages, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if(id == android.R.id.home){
                finish();
            }
            return true;
        }


    // LISTENERS
        public void onEvent( final PushMessage pushMessage ){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pushMessage.getListenerLabel().equalsIgnoreCase(PM_MessagesActivity.class.getName())) {

                        if (pushMessage.getCode() == NEW_MESSAGE_CODE
                                && pushMessage.getBundle() != null
                                && pushMessage.getBundle().getParcelable(Message.MESSAGE_KEY) != null) {

                            Message m = pushMessage.getBundle().getParcelable(Message.MESSAGE_KEY);

                            if( (m.getUserFrom().getId() == mUserFrom.getId()
                                    && m.getUserTo().getId() == mUserTo.getId())
                                || (m.getUserFrom().getId() == mUserTo.getId()
                                    && m.getUserTo().getId() == mUserFrom.getId()) ){

                                MessageAdapter adapter = (MessageAdapter) mRecyclerView.getAdapter();
                                adapter.addListItem(m, 0);

                                mRecyclerView.setVisibility(View.VISIBLE);
                                mRecyclerView.smoothScrollToPosition(0);

                                tvEmptyList.setVisibility(View.GONE);

                                // UPDATE WAS READ IN SERVER TOO
                                    if( m.getUserTo().getId() == mUserFrom.getId() ){
                                        LinkedList<Message> l = new LinkedList<>();
                                        l.add(m);
                                        callUpdateMessageWasRead(l);
                                    }
                            }
                        }
                        else if (pushMessage.getCode() == MESSAGE_WAS_READ_CODE
                                && pushMessage.getBundle() != null
                                && pushMessage.getBundle().getParcelable(Message.MESSAGE_KEY) != null) {

                            Message m = pushMessage.getBundle().getParcelable(Message.MESSAGE_KEY);
                            int position = getItemListPosition(mList, m);

                            if( m.getUserTo().getId() == mUserTo.getId()
                                    && position > -1 ){

                                mList.get( position ).setWasRead(1);

                                MessageAdapter adapter = (MessageAdapter) mRecyclerView.getAdapter();
                                adapter.notifyItemChanged( position );
                            }
                        }
                        else if (pushMessage.getCode() == MESSAGE_REMOVED_CODE
                                && pushMessage.getBundle() != null
                                && pushMessage.getBundle().getParcelable(Message.MESSAGE_KEY) != null) {

                            Message m = pushMessage.getBundle().getParcelable(Message.MESSAGE_KEY);

                            if( (m.getUserFrom().getId() == mUserFrom.getId()
                                    && m.getUserTo().getId() == mUserTo.getId())
                                    || (m.getUserFrom().getId() == mUserTo.getId()
                                    && m.getUserTo().getId() == mUserFrom.getId()) ){

                                int position = getItemListPosition(mList, m);

                                if( position > -1 ){
                                    MessageAdapter adapter = (MessageAdapter) mRecyclerView.getAdapter();
                                    adapter.removeListItem( position );
                                }

                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            if( !etMessage.getText().toString().trim().isEmpty() ){
                etMessage.setEnabled(false);
                btSendMessage.setEnabled(false);

                // CONNETION - GET MESSAGES
                    mMethod = Message.METHOD_SAVE;
                    NetworkConnection.getInstance(this).execute( this, PM_MessagesActivity.class.getName() );
            }
        }


    // NETWORK
        @Override
        public WrapObjToNetwork doBefore() {
            mFlPbLoad.setVisibility(View.VISIBLE);

            if( Util.verifyConnection(this) ){
                Message message;

                if( !mMethod.equalsIgnoreCase( Message.METHOD_LOAD_OLD ) ){
                    message = new Message();
                    message.setMessage( etMessage.getText().toString() );
                    message.setUserFrom(mUserFrom);
                    message.setUserTo(mUserTo);
                }
                else{
                    message = mList.get( mList.size() - 1 );
                }

                return( new WrapObjToNetwork( message, mMethod ) );
            }
            return null;
        }

        @Override
        public void doAfter(JSONObject jsonObject) {
            mFlPbLoad.setVisibility(View.GONE );
            etMessage.setEnabled(true);
            btSendMessage.setEnabled(true);

            if( jsonObject != null ){
                MessageAdapter adapter = (MessageAdapter) mRecyclerView.getAdapter();
                Gson gson = new Gson();

                try{
                    if( !jsonObject.isNull("messages") ){
                        LinkedList<Message> listNotRead = new LinkedList<>();
                        JSONArray jsonArray = jsonObject.getJSONArray("messages");

                        for(int i = 0, tamI = jsonArray.length(); i < tamI; i++){
                            Message m = gson.fromJson( jsonArray.getJSONObject( i ).toString(), Message.class );
                            m.setRegTime(m.getRegTime() * 1000); // IN MILLISECONDS

                            if( m.getUserTo().getId() == mUserFrom.getId()
                                    && m.getWasRead() == 0 ){
                                listNotRead.add(m);
                            }

                            adapter.addListItem(m, mList.size() );
                        }

                        if( jsonArray.length() == 0 ){ // AVOID TO KEEP LOADING
                            mIsLastItem = true;
                        }

                        callUpdateMessageWasRead( listNotRead );
                    }
                    else {
                        boolean status = jsonObject.getBoolean("result");

                        // CLEANING EDITE TEXT, SINCE MESSAGE WAS DELIVERED CORRECTLY
                        if( status ){
                            etMessage.setText("");
                        }
                        else{
                            throw new Exception();
                        }
                    }
                }
                catch(Exception e){
                    Toast.makeText(this, "Falhou. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Falhou. Tente novamente.", Toast.LENGTH_SHORT).show();
            }


            // WHEN NO USERS ARE CONNECTED
                mRecyclerView.setVisibility(mList.isEmpty() ? View.GONE : View.VISIBLE);
                if( mList.isEmpty() ){
                    tvEmptyList.setVisibility(View.VISIBLE);
                }
                else {
                    tvEmptyList.setVisibility(View.GONE);
                }
        }
}
