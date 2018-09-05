package com.abadie.moran.fivewords;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private boolean choosing_letter = true;
    private EditText editText;
    private TextView text_choose;
    private TextView text_adv;
    private View edit_text_box;
    private Button button;
    private String letter;
    private View[][] boxes = new View[5][5];
    private TextView[][] boxes_letters = new TextView[5][5];
    private String[][] letters = new String[5][5];
    private int old_i = -1;
    private int old_j = -1;
    private int id_game = 0;
    private String public_key_1 = "";
    private String public_key_0 = "";
    private String login_connected = "";
    private String password_connected = "";
    private String crypted_password = "";
    private View waitingScreen;
    private RequestQueue queue;
    private String current_letter;
    private Intent intent_loged_in;
    private ConstraintLayout grid_view;

    private TextView playag;
    private boolean your_turn_to_play = true;
    private final String url_read_game = "http://10.0.2.2:8000/read_game/";
    private final String url_send_letter = "http://10.0.2.2:8000/send_letter/";
    private final String url_send_letter_grid = "http://10.0.2.2:8000/send_letter_grid/";
    private boolean waiting_for_other = false;
    private boolean game_finished = false;
    private Handler h = new Handler();
    private int delay = 3000;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.textinputval);
        text_choose = findViewById(R.id.textView);
        edit_text_box = findViewById(R.id.textinputcho);
        button = findViewById(R.id.button);
        waitingScreen = findViewById(R.id.WaitingScreen);
        text_adv = findViewById(R.id.adversaire);
        queue = Volley.newRequestQueue(this);
        playag = findViewById(R.id.playag);
        intent_loged_in = new Intent(this, LogedIn.class);
        read_boxes();
        grid_view = findViewById(R.id.grid_view);
    }

    @Override
    public void onResume() {
        queue.cancelAll(this);
        waitingScreen.setVisibility(View.VISIBLE);
        login_connected = getIntent().getStringExtra("LOGIN");
        password_connected = getIntent().getStringExtra("PASSWORD");
        public_key_0 = getIntent().getStringExtra("public_key_0");
        public_key_1 = getIntent().getStringExtra("public_key_1");
        id_game = Integer.parseInt(getIntent().getStringExtra("id_game"));
        crypted_password = RSA.crypt(password_connected, public_key_0, public_key_1);
        super.onResume();
        update_game();
        h.postDelayed( runnable = new Runnable() {
            public void run() {

                if (!game_finished && waiting_for_other) {
                    update_game();


                }
                h.postDelayed(runnable, delay);
            }
        }, delay);
    }
    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
    public void return_click(View view) {
        intent_loged_in.putExtra("LOGIN", login_connected);
        intent_loged_in.putExtra("PASSWORD", password_connected);
        intent_loged_in.putExtra("public_key_0", public_key_0);
        intent_loged_in.putExtra("public_key_1", public_key_1);


        startActivity(intent_loged_in);
    }
    private void clear_grid() {
        grid_view.removeAllViews();
        int j;
        int i;
        for (i = 0; i < 5; i++) {
            for (j = 0; j < 5; j++) {
                boxes_letters[i][j].setText("");
                boxes_letters[i][j].setTextColor(Color.BLACK);
                GradientDrawable border = new GradientDrawable();
                border.setColor(Color.parseColor("#FFC4A5")); //white background
                border.setStroke(1, 0xFF000000); //black bor

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    boxes_letters[i][j].setBackgroundDrawable(border);
                } else {
                    boxes_letters[i][j].setBackground(border);
                }


                letters[i][j] = "";
            }
        }
    }
    private void updategrid(int i, int j, String letter) {
        boxes_letters[i][j].setText(letter);
        letters[i][j] = letter;
    }
    public void update_game_from_json(JSONObject obj) {
        try {

            boolean error_occured = (boolean) obj.get("error");

            if (!error_occured) {

                your_turn_to_play= (boolean) obj.get("yourturn");
                JSONArray grid = (JSONArray) obj.get("grid");
                clear_grid();
                current_letter = (String) obj.get("letter");
                waiting_for_other = (boolean) obj.get("waiting_for_other");
                game_finished= (boolean) obj.get("game_finished");
                JSONObject data_finish= (JSONObject) obj.get("game_fi");
                letter = current_letter;
                for (int i = 0 ; i < grid.length(); i++) {
                    JSONObject friend_json = grid.getJSONObject(i);
                    updategrid((int) friend_json.get("row"), (int) friend_json.get("col")
                            , (String) friend_json.get("letter"));



                }

                choosing_letter = false;
                if (current_letter.length() == 0 && your_turn_to_play) {
                    choosing_letter = true;

                }
                if (game_finished) {

                    game_finished(data_finish, (String) obj.get("play_against"));
                } else if (choosing_letter) {
                    show_choose();

                }else if (waiting_for_other) {
                    wait_for_other();
                }else {
                    hide_choose();
                }


                playag.setText("Vous jouez contre " + obj.get("play_against"));
                waitingScreen.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {

        }
    }
    public void update_game() {

        StringRequest postRequest = new StringRequest(Request.Method.POST, url_read_game,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);

                            update_game_from_json(obj);
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
                params_post.put("id_game", Integer.toString(id_game));

                return params_post;
            }
        };
        queue.add(postRequest);
    }
    public void send_new_letter(String message) {
        final String mess = message;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_send_letter,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            update_game_from_json(obj);
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
                params_post.put("letter", mess);
                params_post.put("id_game", Integer.toString(id_game));

                return params_post;
            }
        };
        queue.add(postRequest);
    }
    public void send_letter_grid() {
        final String mess = letter;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url_send_letter_grid,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            update_game_from_json(obj);
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
                params_post.put("letter", mess);
                params_post.put("id_game", Integer.toString(id_game));
                params_post.put("i", Integer.toString(old_i));
                params_post.put("j", Integer.toString(old_j));

                return params_post;
            }
        };
        queue.add(postRequest);
    }
    public void sendMessage(View view) {
        if (!waiting_for_other) {
            if (choosing_letter) {
                String message = editText.getText().toString();
                if (message.length() == 1) {
                    send_new_letter(message);




                }
            }else {
                if (old_i >= 0 && old_j >= 0) {
                    letters[old_i][old_j] = letter;
                    send_letter_grid();
                }


            }
        }
    }
    private void read_boxes() {
        int j;
        int i;
        for (i = 0; i < 5; i++) {
            for (j = 0; j < 5; j++) {
                final int finalI = i;
                final int finalJ = j;
                int resID = getResources().getIdentifier("box" + Integer.toString(i) + Integer.toString(j), "id", getPackageName());
                boxes[i][j] =findViewById(resID);
                resID = getResources().getIdentifier("boxText" + Integer.toString(i) + Integer.toString(j), "id", getPackageName());
                boxes_letters[i][j] =findViewById(resID);
                boxes_letters[i][j].setTextSize(20);

                letters[i][j] = "";


                boxes[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if (!choosing_letter && !waiting_for_other) {
                            if (letters[finalI][finalJ].length() == 0) {
                                if (old_i == finalI && finalJ == old_j) {
                                    boxes_letters[finalI][finalJ].setText("");
                                    boxes_letters[finalI][finalJ].setTextColor(Color.BLACK);
                                    old_i = -1;
                                    old_j = -1;
                                }else {
                                    boxes_letters[finalI][finalJ].setText(letter.toUpperCase());
                                    boxes_letters[finalI][finalJ].setTextColor(Color.RED);
                                    if (old_i >= 0 && old_j >= 0) {
                                        boxes_letters[old_i][old_j].setText("");
                                        boxes_letters[old_i][old_j].setTextColor(Color.BLACK);
                                    }
                                    old_i = finalI;
                                    old_j = finalJ;
                                }

                            }

                        }
                    }
                });


            }
        }
    }
    private void hide_choose() {
        editText.setVisibility(View.INVISIBLE);
        text_adv.setVisibility(View.INVISIBLE);
        text_choose.setVisibility(View.VISIBLE);
        text_choose.setText("Ajouter : " + current_letter.toUpperCase());
        edit_text_box.setVisibility(View.INVISIBLE);
        button.setVisibility(View.VISIBLE);
        old_i = -1;
        old_j = -1;
        // TODO kill screen
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(waitingScreen.getWindowToken(), 0);

    }
    private void game_finished(JSONObject data_finish, String name_other) throws JSONException {
        JSONObject my = (JSONObject) data_finish.get("my");
        JSONObject other = (JSONObject) data_finish.get("other");
        int my_points = (int) my.get("points");
        int other_points = (int) other.get("points");
        editText.setVisibility(View.INVISIBLE);
        text_adv.setVisibility(View.VISIBLE);
        String text = " Vous : "  + Integer.toString(my_points) + " - " + name_other
                + " : " + Integer.toString(other_points);
        if (my_points > other_points) {
            text_adv.setTextColor(Color.GREEN);
            text += " ; " + " Vous avez gagné";
        }else if (my_points == other_points) {
            text_adv.setTextColor(Color.GRAY);
            text += " ; " + " égalité !";
        }else {
            text_adv.setTextColor(Color.RED);
            text += " ; " + " Perdu.";
        }
        text_adv.setText(text);
        text_choose.setVisibility(View.INVISIBLE);
        edit_text_box.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        // TODO kill screen
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(waitingScreen.getWindowToken(), 0);
        old_i = -1;
        old_j = -1;

        JSONArray grid = (JSONArray) my.get("dots");
        ArrayList<int[]> list = new ArrayList<int[]>();
        for (int i = 0 ; i < grid.length(); i++) {
            JSONObject friend_json = grid.getJSONObject(i);

            boolean is_line = (boolean) friend_json.get("line");

            int fro = (int) friend_json.get("i");

            int to = (int) friend_json.get("j");

            int index_line = (int) friend_json.get("index_line");


            for (int k = fro; k <= to; k++) {
                GradientDrawable border = new GradientDrawable();

                border.setStroke(1, 0xFF000000);
                int new_i;
                int new_j;
                if (is_line) {
                    border.setColor(Color.parseColor("#C28EF2"));
                    new_i = k;
                    new_j = index_line;

                }else {
                    new_i = index_line;
                    new_j = k;

                    border.setColor(Color.parseColor("#AF59FF"));
                }
                boolean found = false;
                for (int[] val : list) {
                    if (val[0] == new_i && val[1] == new_j) {
                        Log.d("yay", "yayayayayay");
                        border.setColor(Color.parseColor("#8400FF"));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    int[] vals = new int[2];
                    vals[0] = new_i;
                    vals[1] = new_j;
                    list.add(vals);
                }

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    boxes_letters[new_i][new_j].setBackgroundDrawable(border);
                } else {
                    boxes_letters[new_i][new_j].setBackground(border);
                }


            }

        }





    }

    private void wait_for_other() {
        editText.setVisibility(View.INVISIBLE);
        text_adv.setVisibility(View.VISIBLE);
        text_adv.setText("C'est à votre adversaire de jouer");
        text_adv.setTextColor(Color.GRAY);
        text_choose.setVisibility(View.INVISIBLE);
        edit_text_box.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        // TODO kill screen
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(waitingScreen.getWindowToken(), 0);
        old_i = -1;
        old_j = -1;

    }
    private void show_choose() {
        editText.setVisibility(View.VISIBLE);
        text_adv.setVisibility(View.INVISIBLE);
        text_choose.setVisibility(View.VISIBLE);
        text_choose.setText("Veuillez ajouter :");
        edit_text_box.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);

    }


}
