package br.com.thiengo.gcmexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import org.json.JSONObject;

import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.domain.WrapObjToNetwork;
import br.com.thiengo.gcmexample.extra.Util;
import br.com.thiengo.gcmexample.network.NetworkConnection;
import br.com.thiengo.gcmexample.network.Transaction;

public class PM_NicknameActivity extends AppCompatActivity implements Transaction {
    private static final String TAG = "LOG";

    public static final int CODE = 6548; // ANY INT

    private TextInputLayout tilNickname;
    private EditText etNickname;
    private View flProxy;
    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm__nickname);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.tb_main);
        mToolbar.setTitle("Configurações");

        setSupportActionBar(mToolbar);
        if( getSupportActionBar() != null ){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        if( getIntent() != null
                && getIntent().getExtras() != null
                && getIntent().getExtras().getParcelable(User.USER_KEY) != null ){

            mUser = getIntent().getExtras().getParcelable(User.USER_KEY);
        }
        else{
            finish();
        }

        flProxy = findViewById(R.id.fl_proxy);
        tilNickname = (TextInputLayout) findViewById(R.id.til_nickname);
        etNickname = (EditText) findViewById(R.id.et_nickname);
        etNickname.setText( mUser.getNickname() );
    }

    @Override
    protected void onStop() {
        super.onStop();

        // STOP CONNECTION
        NetworkConnection.getInstance(this).getRequestQueue().cancelAll( PM_NicknameActivity.class.getName() );
    }


    @Override
    public void onBackPressed() {
        if( getIntent() != null ){
            Bundle bundle = new Bundle();
            bundle.putParcelable(User.USER_KEY, mUser);

            Intent intent = getIntent();
            intent.putExtras( bundle );

            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }


    // MENU
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_pm__nickname, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if(id == android.R.id.home){
                finish();
            }
            if( id == R.id.action_send ){
                boolean hasError = false;
                if( etNickname.getText().toString().trim().length() == 0 ){
                    tilNickname.setErrorEnabled(true);
                    tilNickname.setError("Informe um nickname");
                    hasError = true;
                }
                else{
                    tilNickname.setErrorEnabled(false);
                }

                if( !hasError ){
                    mUser.setNickname(etNickname.getText().toString());

                    // SEND DATA
                    NetworkConnection.getInstance(this).execute( this, PM_NicknameActivity.class.getName() );
                }
            }
            return true;
        }



    // NETWORK
        @Override
        public WrapObjToNetwork doBefore() {
            flProxy.setVisibility(View.VISIBLE);

            if( Util.verifyConnection(this) ){
                return( new WrapObjToNetwork(mUser, "update-nickname" ) );
            }
            return null;
        }


        @Override
        public void doAfter(JSONObject jsonObject) {
            flProxy.setVisibility(View.GONE);

            if(jsonObject != null){
                try{
                    Gson gson = new Gson();
                    boolean result = jsonObject.getBoolean("result");

                    mUser.setNickname( result ? mUser.getNickname() : "" );

                    android.support.design.widget.Snackbar.make(findViewById(R.id.cl_container),
                            result ? "Arualizado" : "Falhou, tente novamente.",
                            android.support.design.widget.Snackbar.LENGTH_LONG)
                            .show();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            else{
                android.support.design.widget.Snackbar.make(findViewById(R.id.cl_container),
                        "Falhou, tente novamente.",
                        android.support.design.widget.Snackbar.LENGTH_LONG)
                        .show();
            }
        }
}
