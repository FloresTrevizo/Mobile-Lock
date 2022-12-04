package com.example.mobilelock;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.os.Handler;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.widget.Spinner;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class WeeklyOutlook extends AppCompatActivity {

    private int progressStatus = 0;
    private Handler handler = new Handler();
    private boolean userSelect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_outlook);
        //Navigation Code
        Spinner spinner=findViewById(R.id.spinner2);
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
                            startActivity(new Intent(WeeklyOutlook.this, HomeScreen.class));
                            break;
                        case 1:
                            startActivity(new Intent(WeeklyOutlook.this, HomeScreen.class));
                            break;
                        case 2:
                            startActivity(new Intent(WeeklyOutlook.this, WeeklyOutlook.class));
                            break;
                        case 3:
                            startActivity(new Intent(WeeklyOutlook.this, MainActivity.class));
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
        //Calendar and progress bar update code
        ProgressBar mondayBar = (ProgressBar) findViewById(R.id.mondayBar);
        ProgressBar tuesdayBar = (ProgressBar) findViewById(R.id.tuesdayBar);
        ProgressBar wednesdayBar = (ProgressBar) findViewById(R.id.wednesdayBar);
        ProgressBar thursdayBar = (ProgressBar) findViewById(R.id.thursdayBar);
        ProgressBar fridayBar = (ProgressBar) findViewById(R.id.fridayBar);
        ProgressBar saturdayBar = (ProgressBar) findViewById(R.id.saturdayBar);
        ProgressBar sundayBar = (ProgressBar) findViewById(R.id.sundayBar);
        // Start long running operation in a background thread

        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long end = System.currentTimeMillis();

        for (int i = 0; i <= 6; i++){
            long start = calendar.getTimeInMillis() - TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY));
            String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);;
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
            int totalTime = 0;
            for(UsageStats usageStat:stats) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    totalTime += usageStat.getTotalTimeVisible();
                }
            }

            end = start;
            calendar.add((Calendar.DAY_OF_YEAR), -1);

            switch (dayOfWeek) {
                case "Friday":
                    fridayBar.setProgress((int) TimeUnit.MILLISECONDS.toHours(totalTime));
                    totalTime = 0;
                    break;
                case "Saturday":
                    saturdayBar.setProgress((int) TimeUnit.MILLISECONDS.toHours(totalTime));
                    totalTime = 0;
                    break;
                case "Sunday":
                    sundayBar.setProgress((int) TimeUnit.MILLISECONDS.toHours(totalTime));
                    totalTime = 0;
                    break;
                case "Monday":
                    mondayBar.setProgress((int) TimeUnit.MILLISECONDS.toHours(totalTime));
                    totalTime = 0;
                    break;
                case "Tuesday":
                    tuesdayBar.setProgress((int) TimeUnit.MILLISECONDS.toHours(totalTime));
                    totalTime = 0;
                    break;
                case "Wednesday":
                    wednesdayBar.setProgress((int) TimeUnit.MILLISECONDS.toHours(totalTime));
                    totalTime = 0;
                    break;
                case "Thursday":
                    thursdayBar.setProgress((int) TimeUnit.MILLISECONDS.toHours(totalTime));
                    totalTime = 0;
                    break;
            }
        }
    }
}