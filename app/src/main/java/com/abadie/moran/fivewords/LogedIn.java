package com.abadie.moran.fivewords;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LogedIn extends AppCompatActivity {
    private String login_connected = "";
    private String password_connected = "";
    private Intent intent_start;
    private TextView hello_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loged_in);
        intent_start = new Intent(this, Start.class);
        hello_text = findViewById(R.id.hello_text);
    }
    @Override
    public void onStart() {
        super.onStart();

        login_connected= getIntent().getStringExtra("LOGIN");
        password_connected = getIntent().getStringExtra("PASSWORD");
        hello_text.setText("Bonjour " + login_connected);
        Log.d("dddd", login_connected);
    }
    public void logout(View view) {
        try {
            CacheDir.writeAllCachedText(getApplicationContext(),  "USERDATA",
                    "");
            startActivity(intent_start);

        }finally {

        }
    }

}
