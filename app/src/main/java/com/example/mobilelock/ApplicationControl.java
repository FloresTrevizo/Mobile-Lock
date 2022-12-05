package com.example.mobilelock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApplicationControl extends AppCompatActivity {

    private boolean userSelect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_control);
        //Navigation Bar
        Spinner spinner=findViewById(R.id.spinner4);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userSelect = true;
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                if (userSelect) {
                    switch (position) {
                        case 0:
                            startActivity(new Intent(ApplicationControl.this, HomeScreen.class));
                            break;
                        case 1:
                            startActivity(new Intent(ApplicationControl.this, HomeScreen.class));
                            break;
                        case 2:
                            startActivity(new Intent(ApplicationControl.this, WeeklyOutlook.class));
                            break;
                        case 3:
                            startActivity(new Intent(ApplicationControl.this, MainActivity.class));
                            break;
                    }
                    userSelect = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                return;
            }
        });
        //THIS IS THE CODE FOR THE SWITCHES (FOR PABLO)!!!!!!!!!!!!!!!!
        Switch switch1 = findViewById(R.id.switch1);
        Switch switch2 = findViewById(R.id.switch3);
        Switch switch3 = findViewById(R.id.switch4);
        Switch switch4 = findViewById(R.id.switch5);

        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long end = System.currentTimeMillis();
        long start = calendar.getTimeInMillis() - TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY));
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);

        UsageStats usageStat1 = null;
        UsageStats usageStat2 = null;
        UsageStats usageStat3 = null;
        UsageStats usageStat4 = null;
        UsageStats[] usageStatArray = {usageStat1,usageStat2,usageStat3,usageStat4};

        for (int i = 0; i < 4; i++) {
            long maxTime = 0;
            for(UsageStats stat: stats) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    long currTime = stat.getTotalTimeVisible();
                    if (currTime > maxTime && !Arrays.stream(usageStatArray).anyMatch(x -> x == stat)) {
                        usageStatArray[i] = stat;
                    }
                }
            }
        }
        switch1.setText(usageStatArray[0].getPackageName());
        switch2.setText(usageStatArray[1].getPackageName());
        switch3.setText(usageStatArray[2].getPackageName());
        switch4.setText(usageStatArray[3].getPackageName());

    }
}