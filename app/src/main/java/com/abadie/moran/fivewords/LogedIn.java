package com.abadie.moran.fivewords;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LogedIn extends AppCompatActivity {
    private String login_connected = "";
    private String password_connected = "";
    private Intent intent_start;
    private TextView hello_text;
    private EditText new_friend;
    private TextView error_message_friend;
    private String new_friend_login;
    private RequestQueue queue ;
    private String public_key_1 = "";
    private String public_key_0 = "";
    private final String url_add = "http://10.0.2.2:8000/add_friend/";;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loged_in);
        intent_start = new Intent(this, Start.class);
        hello_text = findViewById(R.id.hello_text);
        new_friend = findViewById(R.id.new_friend_text);
        error_message_friend = findViewById(R.id.error_message);
        queue = Volley.newRequestQueue(this);
    }
    @Override
    public void onStart() {
        super.onStart();

        login_connected= getIntent().getStringExtra("LOGIN");
        password_connected = getIntent().getStringExtra("PASSWORD");
        public_key_0 = getIntent().getStringExtra("public_key_0");
        public_key_1 = getIntent().getStringExtra("public_key_1");
        hello_text.setText("Bonjour " + login_connected);

    }
    public void logout(View view) {
        try {
            CacheDir.writeAllCachedText(getApplicationContext(),  "USERDATA",
                    "");
            startActivity(intent_start);

        }finally {

        }
    }
    public void click_add_friend(View view) {
        new_friend_login = new_friend.getText().toString();
        error_message_friend.setText("");
        if (new_friend_login != null && new_friend_login != "") {

            final String final_key = public_key_1;

            final String crypted_login = login_connected;

            final String crypted_password = RSA.crypt(password_connected, public_key_0, public_key_1);

            StringRequest postRequest = new StringRequest(Request.Method.POST, url_add,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject obj = new JSONObject(response);
                                try {
                                    boolean error_occured = (boolean) obj.get("error");
                                    String error_mess = (String) obj.get("message");
                                    error_message_friend.setText(error_mess);
                                } finally {

                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }



                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {

                    HashMap<String, String> params_post = new HashMap<String, String>();

                    params_post.put("login", crypted_login);

                    params_post.put("password", crypted_password);
                    params_post.put("public_key", final_key);
                    params_post.put("friend", new_friend_login);
                    return params_post;
                }
            };
            queue.add(postRequest);
        }
    }

}
