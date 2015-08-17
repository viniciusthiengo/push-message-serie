package br.com.thiengo.gcmexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import br.com.thiengo.gcmexample.domain.PushMessage;
import de.greenrobot.event.EventBus;

public class SecondActivity extends AppCompatActivity {
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        tvContent = (TextView) findViewById(R.id.tv_content);
        if( getIntent() != null ){
            tvContent.setText( getIntent().getStringExtra("data1") );
        }
    }
}
