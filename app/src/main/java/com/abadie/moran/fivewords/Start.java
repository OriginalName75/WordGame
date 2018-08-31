package com.abadie.moran.fivewords;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Start extends AppCompatActivity {
    private String url_check ="http://10.0.2.2:8000/check_user/";
    private RequestQueue queue ;
    private TextView error_text;
    private EditText login;
    private EditText password;
    private Intent intent_loged_in;
    private String login_connected = "";
    private String password_connected = "";
    private String public_key_0 = "";
    private String public_key_1 = "";
    private boolean public_key_loaded;
    private String login_crypted = "";
    private String url_key = "http://10.0.2.2:8000/get_key/";
    private Map<String, String>  params_post;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        queue = Volley.newRequestQueue(this);
        error_text = findViewById(R.id.error_text);
        login = findViewById(R.id.login_val);
        password = findViewById(R.id.password_val);
        intent_loged_in = new Intent(this, LogedIn.class);

    }
    @Override
    public void onStart() {
        super.onStart();
        try {
            String content = CacheDir.readAllCachedText(getApplicationContext(),  "USERDATA");
            if (content != null && content.length() > 0) {
                String[] split_lines = content.split("\n");
                if (split_lines.length == 2) {

                    connect_cache(split_lines[0],split_lines[1]);

                }
            }

        }finally {

        }

    }
    private void saveUserData() {
        try {
            CacheDir.writeAllCachedText(getApplicationContext(),  "USERDATA",
                    login_connected+"\n" + password_connected);
        }finally {

        }



    }
    private void go_to_user_act() {
        intent_loged_in.putExtra("LOGIN", login_connected);
        intent_loged_in.putExtra("PASSWORD", password_connected);



        startActivity(intent_loged_in);
    }
    private void json_reader_connexion(JSONObject response, boolean _is_cache) throws JSONException {
        if ((boolean) response.get("answer")) {

            if (!_is_cache) {
                saveUserData();
            }


            go_to_user_act();

        }else {
            if (!_is_cache) {
                error_text.setText((String) response.get("error"));
            }
        }

    }
    public void connect_cache(String _login, String _password) {
        connect_cache_aux_2(_login, _password,true);
    }
    private void connect_cache_aux_2(String _login, String _password, boolean is_cache) {
        login_connected = _login;
        password_connected = _password  ;
        final String login_f = _login;
        final String pass_f = _password;
        final boolean isCacheFinal = is_cache;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url_key,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            public_key_0 = (String) obj.get("public_0");
                            public_key_1 = (String) obj.get("public_1");
                            connect_cache_aux(login_f, pass_f, public_key_0, public_key_1, isCacheFinal);


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
        );
        queue.add(postRequest);
    }
    public void connect_cache_aux(String _login, String _password, String public_key_0, String public_key_1, boolean is_cache) {


        try {
            final String final_key = public_key_1;

            final String crypted_login = _login;
            final boolean isCacheFinal = is_cache;

            final String crypted_password = RSA.crypt(_password, public_key_0, public_key_1);

            StringRequest postRequest = new StringRequest(Request.Method.POST, url_check,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject obj = new JSONObject(response);
                                json_reader_connexion(obj, isCacheFinal);
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

                    return params_post;
                }
            };
            queue.add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    public void connect(View view) {
        error_text.setText("");
        connect_cache_aux_2(login.getText().toString(), password.getText().toString(),false);


    }
    public void register(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://10.0.2.2:8000/register/"));
        startActivity(browserIntent);


    }
}
