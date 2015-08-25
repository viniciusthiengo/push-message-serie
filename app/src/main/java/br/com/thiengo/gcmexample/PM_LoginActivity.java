package br.com.thiengo.gcmexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import br.com.thiengo.gcmexample.domain.Message;
import br.com.thiengo.gcmexample.domain.User;

public class PM_LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "LOG";
    public static final String PREF_KEY_NICKNAME = "br.com.thiengo.gcmexample.Key.Nickname";
    public static final String PREF_KEY_ID = "br.com.thiengo.gcmexample.Key.Id";


    private TextInputLayout tilLogin;
    private EditText etLogin;
    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm__login);

        mUser = new User();

        tilLogin = (TextInputLayout) findViewById(R.id.til_login);
        etLogin = (EditText) findViewById(R.id.et_login);

        Button btLogin = (Button) findViewById(R.id.bt_login);
        btLogin.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // IF USER ALREADY REGISTERED HIS NICKNAME, SO THE SCRIPT KEEP GOING DIRECTLY TO USERS ACTIVITY
            mUser.setId( Long.parseLong(retrievePrefKeyValue(getApplicationContext(), PREF_KEY_ID, "0")) );
            if( mUser.getId() > 0 ){
                mUser.setNickname(retrievePrefKeyValue(getApplicationContext(), PREF_KEY_NICKNAME));

                callNextActivity( 0 );
            }
    }



    private void callNextActivity(int isFirstTime){
        Bundle bundle = new Bundle();
        bundle.putParcelable(User.USER_KEY, mUser);
        bundle.putInt(User.FIRST_TIME_KEY, isFirstTime);

        Intent intent = new Intent( this, PM_UsersActivity.class );

        if( getIntent() != null
                && getIntent().getExtras() != null
                && getIntent().getExtras().getParcelable(Message.MESSAGE_KEY) != null ){

            intent.putExtras( getIntent().getExtras() );
        }
        else{
            intent.putExtras( bundle );
        }

        startActivity( intent );
    }



    // SHARED PREFERENCES
        public static void savePrefKeyValue( Context context, String key, String value ){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            SharedPreferences.Editor e = sp.edit();
            e.putString( key, value );
            e.apply();
        }
        public static String retrievePrefKeyValue( Context context, String key, String... defaultValue ){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
            String dValue = defaultValue != null && defaultValue.length > 0 ? defaultValue[0] : "";
            sp.getString(key, dValue );
            return( sp.getString( key, dValue ) );
        }



    // LISTENERS
        @Override
        public void onClick(View v) {
            mUser.setNickname(etLogin.getText().toString().trim());
            tilLogin.setErrorEnabled(false);

            if( mUser.getNickname().isEmpty() ){
                tilLogin.setErrorEnabled(true);
                tilLogin.setError("Informe um nickname");
            }

            // IF NO ERROR BEING SHOWN, JUST GO TO USERS ACTIVTY
                if( !tilLogin.isErrorEnabled() ){
                    savePrefKeyValue( getApplicationContext(), PREF_KEY_NICKNAME, mUser.getNickname());
                    callNextActivity( 1 );
                }
        }
}
