package com.example.mobilelock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class HomeScreen<adapter> extends AppCompatActivity {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String TAG = "HomeActivity";
    private static int notificationId = 0;

    private static final boolean localLOGV = false;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private HomeScreen.UsageStatsAdapter mAdapter;
    private PackageManager mPm;
    private static NotificationCompat.Builder builder;
    private static NotificationManagerCompat notificationManager;

    private boolean userSelect = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        createNotificationChannel();
        Button button = findViewById(R.id.button);
        Spinner spinner=findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        requestPermissions();

        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();
        mAdapter = new UsageStatsAdapter();




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeScreen.this, ReminderBroadcast.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeScreen.this, 0, intent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                long timeAtButtonClick = System.currentTimeMillis();

                //change this variable to whatever delay you want
                long delaySecondsInMillis = 500 * 10;

                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        timeAtButtonClick + delaySecondsInMillis, pendingIntent);
                    }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //SPINNER NAVIGATION CODE
        spinner.setSelection(Adapter.NO_SELECTION,true);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userSelect = true;
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                if (userSelect) {
                    switch (position) {
                        case 0:
                            System.out.println("SUCCESS SUCCESS SUCCESS");
                            startActivity(new Intent(HomeScreen.this, HomeScreen.class));
                            break;
                        case 1:
                            startActivity(new Intent(HomeScreen.this, HomeScreen.class));
                            break;
                        case 2:
                            startActivity(new Intent(HomeScreen.this, WeeklyOutlook.class));
                            break;
                        case 3:
                            startActivity(new Intent(HomeScreen.this, MainActivity.class));
                            break;
                    }
                    userSelect = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
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


    /////////////////////////
    //Screen time stats code

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }


    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int)(b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    // View Holder used when displaying views
    static class AppViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
    }

    class UsageStatsAdapter extends BaseAdapter {
        // Constants defining order for display order
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_APP_NAME = 1;

        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private HomeScreen.UsageTimeComparator mUsageTimeComparator = new HomeScreen.UsageTimeComparator();
        private HomeScreen.AppNameComparator mAppLabelComparator;
        public final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
        public final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

        UsageStatsAdapter() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -5);

            final List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal.getTimeInMillis(), System.currentTimeMillis());
            if (stats == null) {
                return;
            }

            ArrayMap<String, UsageStats> map = new ArrayMap<>();
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                final android.app.usage.UsageStats pkgStats = stats.get(i);

                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String label = appInfo.loadLabel(mPm).toString();
                    mAppLabelMap.put(pkgStats.getPackageName(), label);

                    UsageStats existingStats =
                            map.get(pkgStats.getPackageName());
                    if (existingStats == null) {
                        map.put(pkgStats.getPackageName(), pkgStats);
                    } else {
                        existingStats.add(pkgStats);
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    // This package may be gone.
                }
            }
            mPackageStats.addAll(map.values());

            // Sort list
            mAppLabelComparator = new HomeScreen.AppNameComparator(mAppLabelMap);
            sortList();
        }

        @Override
        public int getCount() {
            return mPackageStats.size();
        }

        @Override
        public UsageStats getItem(int position) {
            return mPackageStats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            HomeScreen.AppViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.usage_stats_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new HomeScreen.AppViewHolder();
                holder.pkgName = (TextView) convertView.findViewById(R.id.package_name);
                holder.usageTime = (TextView) convertView.findViewById(R.id.usage_time);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (HomeScreen.AppViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder
            UsageStats pkgStats = mPackageStats.get(position);
            if (pkgStats != null) {
                String label = mAppLabelMap.get(pkgStats.getPackageName());
                holder.pkgName.setText(label);
                holder.usageTime.setText(
                        DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
            return convertView;
        }

        void sortList(int sortOrder) {
            if (mDisplayOrder == sortOrder) {
                // do nothing
                return;
            }
            mDisplayOrder= sortOrder;
            sortList();
        }
        private void sortList() {
            if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                if (localLOGV) Log.i(TAG, "Sorting by usage time");
                Collections.sort(mPackageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                if (localLOGV) Log.i(TAG, "Sorting by application name");
                Collections.sort(mPackageStats, mAppLabelComparator);
            }
            notifyDataSetChanged();
        }
    }


    private void requestPermissions() {
        List<UsageStats> stats = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        boolean isEmpty = stats.isEmpty();
        if (isEmpty) {
            startActivity(new android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }
}