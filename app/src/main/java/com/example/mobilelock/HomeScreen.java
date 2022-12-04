package com.example.mobilelock;

import static android.os.SystemClock.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class HomeScreen<adapter> extends AppCompatActivity {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String TAG = "HomeActivity";
    private static int notificationId = 0;

    private static NotificationCompat.Builder builder;
    private static NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        createNotificationChannel();
        Button button = findViewById(R.id.button);
        Spinner spinner=findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call background service on
                try {
                    //calendarCall(view);
                    // Create an explicit intent for an Activity in your app
                    notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    ArrayList<Notification> notifs = new ArrayList<>();

                    notifs.add(buildNotification("Busy Day Tomorrow.", "You have a busy day tomorrow. " +
                            "To promote productivity," +
                            " click here to block Instagram"));
                    notifs.add(buildNotification("Warning:", "Your phone usage was larger than normal" +
                            " yesterday. Click here to see specifics."));
                    notifs.add(buildNotification("Take a Break.", "Your calendar shows you currently busy. " +
                            "Focus on that, not your phone."));


                    moveTaskToBack(true);

                    for (Notification ping : notifs) {
                        sleep(2300);
                        notificationManager.notify(notificationId, ping);
                        notificationId++;
                    }

                    Log.d(TAG, "Pressed");

                } catch (Exception e) {
                    Log.d(TAG + " Exception", e.getMessage());
                }
            }

        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //SPINNER NAVIGATION CODE
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                System.out.println(position);
                switch (position) {
                    case 2:
                        startActivity(new Intent(HomeScreen.this, WeeklyOutlook.class));
                        break;
                    case 3:
                        startActivity(new Intent(HomeScreen.this, MainActivity.class));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private Notification buildNotification(String title, String text) {
        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(getApplicationContext(), "Notifications")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        return builder.build();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Notif Channel";
        String description = "Mobile Lock Notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("Notifications", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);


    }
}