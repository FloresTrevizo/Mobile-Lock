package com.example.mobilelock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Button button = findViewById(R.id.homebutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //call background service on
            }
        });
    }
}