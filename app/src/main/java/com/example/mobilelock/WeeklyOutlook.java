package com.example.mobilelock;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.os.Handler;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import java.sql.SQLOutput;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WeeklyOutlook extends AppCompatActivity {

    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_outlook);
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
            String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
            long start = calendar.getTimeInMillis() - end;
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
            int totalTime = 0;
            for(UsageStats usageStat:stats) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    totalTime += usageStat.getTotalTimeVisible();
                }
            }
            System.out.println(dayOfWeek);
            System.out.println((int) TimeUnit.MILLISECONDS.toHours(totalTime));
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