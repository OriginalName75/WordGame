package com.abadie.moran.fivewords;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean choosing_letter = true;
    private EditText editText;
    private TextView text_choose;
    private View edit_text_box;
    private Button button;
    private String letter;
    private View[][] boxes = new View[5][5];
    private TextView[][] boxes_letters = new TextView[5][5];
    private String[][] letters = new String[5][5];
    private int old_i = -1;
    private int old_j = -1;
    private String public_key_1 = "";
    private String public_key_0 = "";
    private String login_connected = "";
    private String password_connected = "";
    private String crypted_password = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.textinputval);
        text_choose = findViewById(R.id.textView);
        edit_text_box = findViewById(R.id.textinputcho);
        button = findViewById(R.id.button);
        read_boxes();
    }

    @Override
    public void onResume() {
        login_connected = getIntent().getStringExtra("LOGIN");
        password_connected = getIntent().getStringExtra("PASSWORD");
        public_key_0 = getIntent().getStringExtra("public_key_0");
        public_key_1 = getIntent().getStringExtra("public_key_1");
        crypted_password = RSA.crypt(password_connected, public_key_0, public_key_1);
        super.onResume();
    }
    public void sendMessage(View view) {
        if (choosing_letter) {
            String message = editText.getText().toString();
            if (message.length() == 1) {

                choosing_letter = false;
                letter = message;
                hide_choose();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                old_i = -1;
                old_j = -1;
            }
        }else {
            if (old_i >= 0 && old_j >= 0) {

                letters[old_i][old_j] = letter;
                boxes_letters[old_i][old_j].setText(letter.toUpperCase());
                boxes_letters[old_i][old_j].setTextColor(Color.BLACK);
                choosing_letter = true;
                show_choose();
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
                        if (!choosing_letter) {
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
        text_choose.setText("Ajouter : " + letter.toUpperCase());
        edit_text_box.setVisibility(View.INVISIBLE);

    }
    private void show_choose() {
        editText.setVisibility(View.VISIBLE);
        text_choose.setText("Veuillez choisir une lettre");
        edit_text_box.setVisibility(View.VISIBLE);

    }


}
