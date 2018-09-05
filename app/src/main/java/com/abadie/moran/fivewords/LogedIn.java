package com.abadie.moran.fivewords;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LogedIn extends AppCompatActivity {
    private String login_connected = "";
    private String password_connected = "";
    private Intent intent_start;
    private Intent intent_game;
    private TextView hello_text;
    private EditText new_friend;
    private TextView error_message_friend;
    private String new_friend_login;
    private String crypted_password;
    private RequestQueue queue ;
    private String public_key_1 = "";
    private String public_key_0 = "";
    private final String url_add = "http://10.0.2.2:8000/add_friend/";
    private final String url_get_new_friend_list = "http://10.0.2.2:8000/get_new_friend_list/";
    private final String url_new_friend_answer = "http://10.0.2.2:8000/new_friend_answer/";

    private LinearLayout lisFriendView;
    private LinearLayout listFriendView;
    private Handler h = new Handler();
    private int delay = 10000;
    private Runnable runnable;
    private TextView are_u_sure;
    private View are_u_sure_window;
    private boolean are_u_sure_mode = false;
    private int id_save = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loged_in);
        intent_start = new Intent(this, Start.class);
        hello_text = findViewById(R.id.hello_text);
        new_friend = findViewById(R.id.new_friend_text);
        error_message_friend = findViewById(R.id.error_message);
        queue = Volley.newRequestQueue(this);
        lisFriendView = findViewById(R.id.FriendListRequest);
        listFriendView = findViewById(R.id.friend_list_id);
        intent_game = new Intent(this, MainActivity.class);
        are_u_sure = findViewById(R.id.are_u_sure);
        are_u_sure_window = findViewById(R.id.are_u_sure_window);
    }
    @Override
    public void onResume() {
        login_connected= getIntent().getStringExtra("LOGIN");
        password_connected = getIntent().getStringExtra("PASSWORD");
        public_key_0 = getIntent().getStringExtra("public_key_0");
        public_key_1 = getIntent().getStringExtra("public_key_1");
        hello_text.setText("Bonjour " + login_connected);
        crypted_password = RSA.crypt(password_connected, public_key_0, public_key_1);
        printFriendsREquests();
        Log.d("rerun", "rerturn");
        h.postDelayed( runnable = new Runnable() {
            public void run() {
                printFriendsREquests();

                h.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();




    }
    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
    public void are_u_sure_yes(View view) {
        if (are_u_sure_mode) {
            are_u_sure_mode = false;
            are_u_sure_window.setVisibility(View.INVISIBLE);
            intent_game.putExtra("LOGIN", login_connected);
            intent_game.putExtra("PASSWORD", password_connected);
            intent_game.putExtra("public_key_0", public_key_0);
            intent_game.putExtra("public_key_1", public_key_1);
            intent_game.putExtra("id_game", Integer.toString(id_save));
            startActivity(intent_game);

        }
    }
    public void are_u_sure_no(View view) {
        if (are_u_sure_mode) {
            are_u_sure_mode = false;
            are_u_sure_window.setVisibility(View.INVISIBLE);
        }

    }

    private void friend_request_response_but(int id, boolean answer, Button b) {
        final int id_f = id;
        final boolean answer_f = answer;
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                friend_request_response(id_f, answer_f);

            }
        });
    }
    private void friend_request_response(int id, boolean answer) {
        final boolean answer_f = answer;
        final int id_f = id;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_new_friend_answer,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        printFriendsREquests();



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

                params_post.put("login", login_connected);

                params_post.put("password", crypted_password);
                params_post.put("public_key", public_key_1);
                params_post.put("id", Integer.toString(id_f));
                params_post.put("answer", Boolean.toString(answer_f));
                return params_post;
            }
        };
        queue.add(postRequest);
    }
    public void logout(View view) {
        try {
            CacheDir.writeAllCachedText(getApplicationContext(),  "USERDATA",
                    "");


            startActivity(intent_start);
        }finally {

        }
    }

    private void start_or_continue_game(int id, Button b, boolean game_started, String name) {
        id_save = id;
        final boolean started = game_started;
        final String name_f = name;
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (started) {
                    intent_game.putExtra("LOGIN", login_connected);
                    intent_game.putExtra("PASSWORD", password_connected);
                    intent_game.putExtra("public_key_0", public_key_0);
                    intent_game.putExtra("public_key_1", public_key_1);
                    intent_game.putExtra("id_game", Integer.toString(id_save));
                    startActivity(intent_game);

                } else {
                    are_u_sure.setText("Voulez vous commencer une partie avec " + name_f + " ?");
                    are_u_sure_window.setVisibility(View.VISIBLE);
                    are_u_sure_mode = true;
                }
            }

        });
    }
    private void add_friend_list_views(int id, String name, double mmr, boolean game_started,
                                       boolean can_play, boolean game_finished) {

        LinearLayout horlay = new LinearLayout(this);
        horlay.setOrientation(LinearLayout.HORIZONTAL);
        horlay.setGravity(Gravity.CENTER);
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFFFFFFF); //white background
        border.setStroke(1, 0xFF000000); //black bor

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            horlay.setBackgroundDrawable(border);
        } else {
            horlay.setBackground(border);
        }

        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                50);

        horlay.setWeightSum(6f);
        horlay.setLayoutParams(LLParams);
        TextView message = new TextView(this);
        message.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 50));
        message.setText("  " + name );
        message.setGravity(Gravity.CENTER);
        message.setTextColor(Color.BLACK);
        TextView message3 = new TextView(this);
        message3.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 50));
        message3.setText(" | mmr : " + Integer.toString((int) mmr));
        message3.setGravity(Gravity.CENTER);
        message3.setTextSize(8);


        Button btnTag = new Button(this);


        btnTag.setTextSize(10);
        btnTag.setPadding(0,0,0,0);

        if (!game_started) {
            btnTag.setText("Commencer partie");
            btnTag.setLayoutParams(new ViewGroup.LayoutParams(170, 50));
        } else if (game_finished) {
            btnTag.setText("Voire score");
            btnTag.setLayoutParams(new ViewGroup.LayoutParams(150, 50));
        }else {
            btnTag.setText("Jouer");
            btnTag.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 50));
        }
        TextView message2 = new TextView(this);
        message2.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 50));

        message2.setGravity(Gravity.CENTER);
        if (game_started) {
            if (game_finished) {
                message2.setText(" | Partie terminée");
                message2.setTextColor(Color.RED);
            } else if (can_play) {
                message2.setText(" | A vous !");
                message2.setTextColor(Color.GREEN);

            }else {
                message2.setText(" | A l'adversaire");
                message2.setTextColor(Color.GRAY);
            }
        }

        message2.setTextSize(10);


        horlay.addView(message);
        horlay.addView(btnTag);
        horlay.addView(message3);
        if (game_started) {
            horlay.addView(message2);
        }

        start_or_continue_game(id, btnTag, game_started, name);

        listFriendView.addView(horlay);


    }
    private void add_new_friend_list_views(int id, String name) {

        LinearLayout horlay = new LinearLayout(this);
        horlay.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFFFFFFF); //white background
        border.setStroke(1, 0xFF009900); //
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            horlay.setBackgroundDrawable(border);
        } else {
            horlay.setBackground(border);
        }

        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);


        horlay.setWeightSum(6f);
        horlay.setLayoutParams(LLParams);
        TextView message = new TextView(this);
        message.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 50));
        message.setText("Demande d'amis de " + name);
        message.setGravity(Gravity.CENTER);
        horlay.addView(message);
        Button btnTag = new Button(this);
        btnTag.setLayoutParams(new ViewGroup.LayoutParams(50, 50));
        btnTag.setText("ok");
        btnTag.setTextSize(8);
        btnTag.setPadding(0,0,0,0);
        friend_request_response_but(id,true, btnTag);
        //add button to the layout
        horlay.addView(btnTag);

        Button btnTag_no = new Button(this);
        btnTag_no.setLayoutParams(new ViewGroup.LayoutParams(50, 50));
        btnTag_no.setText("no");
        btnTag_no.setTextSize(8);
        btnTag_no.setPadding(0,0,0,0);
        friend_request_response_but(id,false, btnTag_no);
        //add button to the layout
        horlay.addView(btnTag_no);

        lisFriendView.addView(horlay);


    }
    private void printFriendsREquests() {

        StringRequest postRequest = new StringRequest(Request.Method.POST, url_get_new_friend_list,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            try {
                                boolean error_occured = (boolean) obj.get("error");
                                if (!error_occured) {

                                    JSONArray list_friend = (JSONArray) obj.get("new_friend_list");
                                    lisFriendView.removeAllViews();
                                    for (int i = 0 ; i < list_friend.length(); i++) {
                                        JSONObject friend_json = list_friend.getJSONObject(i);
                                        add_new_friend_list_views((int) friend_json.get("id"),
                                                (String) friend_json.get("name"));

                                    }
                                    JSONArray list_friend_2 = (JSONArray) obj.get("list_friend");
                                    listFriendView.removeAllViews();
                                    for (int i = 0 ; i < list_friend_2.length(); i++) {
                                        JSONObject friend_json = list_friend_2.getJSONObject(i);
                                        add_friend_list_views((int) friend_json.get("id"),
                                                (String) friend_json.get("name"),
                                                (double) friend_json.get("mmr"),
                                                (boolean) friend_json.get("game_started"),
                                                (boolean) friend_json.get("can_play"),
                                                (boolean) friend_json.get("game_finished"));

                                    }


                                }
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

                params_post.put("login", login_connected);

                params_post.put("password", crypted_password);
                params_post.put("public_key", public_key_1);
                return params_post;
            }
        };
        queue.add(postRequest);


    }



    public void click_add_friend(View view) {
        new_friend_login = new_friend.getText().toString();
        error_message_friend.setText("");
        if (new_friend_login != null && new_friend_login != "") {
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

                    params_post.put("login", login_connected);

                    params_post.put("password", crypted_password);
                    params_post.put("public_key", public_key_1);
                    params_post.put("friend", new_friend_login);
                    return params_post;
                }
            };
            queue.add(postRequest);
        }
    }

}
