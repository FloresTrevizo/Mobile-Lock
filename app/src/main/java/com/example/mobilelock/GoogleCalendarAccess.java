package com.example.mobilelock;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;

//import com.google.android.gms.auth.api.identity.SaveAccountLinkingTokenRequest;

import com.google.api.client.util.DateTime;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.browser.BrowserAllowList;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleCalendarAccess extends AppCompatActivity {
    private Button button;
    private static final int RC_AUTH = 100;
    private AuthorizationService mAuthService;
    private AuthorizationServiceConfiguration mAuthServiceConfig;
    private AuthStateManager mAuthStateManager;
    private ExecutorService mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_uri_receiver);
        mExecutor = Executors.newSingleThreadExecutor();


        button = findViewById(R.id.calendar);
        mAuthStateManager = new AuthStateManager(this);

        AppAuthConfiguration appAuthConfiguration = new AppAuthConfiguration.Builder()
                .setBrowserMatcher(
                        new BrowserAllowList(
                                VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                                VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
                        )
                ).build();
        mAuthService = new AuthorizationService(getApplication(), appAuthConfiguration);


        mAuthServiceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(getString(R.string.URL_AUTHORIZATION)),
                Uri.parse(getString(R.string.URL_TOKEN_EXCHANGE)),
                null,
                Uri.parse(getString(R.string.URL_LOGOUT))
        );

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Google Calendar", "Starting Calendar Call");
                tutorialAuthorization(view);
            }
        });


    }


    private void tutorialAuthorization(View view) {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                mAuthServiceConfig,
                getString(R.string.CLIENT_ID),
                ResponseTypeValues.CODE,
                Uri.parse(getString(R.string.URL_AUTH_REDIRECT))
        );

        builder.setScopes(getString(R.string.SCOPE_PROFILE),
                getString(R.string.SCOPE_EMAIL),
                getString(R.string.SCOPE_OPENID),
                getString(R.string.SCOPE_CALENDAR));

        AuthorizationRequest authRequest = builder.build();

        Log.d("Google Calendar", "Request Sent");
        Intent authIntent = mAuthService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, RC_AUTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("Google Calendar", "Result");
        Log.d("Google Calendar", "" + resultCode);

        if(resultCode == RESULT_OK) {
            AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
            AuthorizationException ex = AuthorizationException.fromIntent(data);

            if(resp != null) {
                mAuthService = new AuthorizationService(this);
                mAuthStateManager.updateAfterAuthorization(resp, ex);

                mAuthService.performTokenRequest(resp.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                            if(response != null) {
                                Log.d("Google Calendar", "" + mAuthStateManager.getCurrent().isAuthorized());
                                mAuthStateManager.updateAfterTokenResponse(response, ex);
                                Log.d("Google Calendar", "" + mAuthStateManager.getCurrent().isAuthorized());

                                //token exchange succeeded
                                HandlerThread handlerThread = new HandlerThread("background-thread");
                                handlerThread.start();

                                Handler handler = new Handler(handlerThread.getLooper());
                                int secondCount = 5;
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        beginCalendarCalls();
                                        handler.postDelayed(this, secondCount * 1000);
                                    }
                                }, secondCount * 1000);
                            }
                        }
                    });
            }
        }

        startActivity(new Intent(GoogleCalendarAccess.this, HomeScreen.class));

    }

    private void beginCalendarCalls() {
        if (mAuthStateManager.getCurrent().isAuthorized()) {
            Log.d("Google Calendar", "Begin Calendar Call");
            mAuthStateManager.getCurrent().performActionWithFreshTokens(mAuthService,
                    this::fetchCalendarInfo);
        }

    }

    private void fetchCalendarInfo(String accessToken,
                                   String idToken, AuthorizationException ex) {
        AuthorizationServiceDiscovery discovery =
                mAuthStateManager.getCurrent()
                        .getAuthorizationServiceConfiguration()
                        .discoveryDoc;


        Uri userInfoEndpoint = Uri.parse(getString(R.string.SCOPE_CALENDAR));

        mExecutor.submit(() -> {
            try {
                DateTime now = new DateTime(System.currentTimeMillis());
                String time = now.toString().substring(0, now.toString().length()-6) + "Z";
                URL url = new URL("https://www.googleapis.com/calendar/v3/calendars/primary/events");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                Log.d("Google Calendar", "Time: " + time);

                conn.setRequestMethod("GET");
                conn.setRequestProperty("calendarId", "primary");
                conn.setRequestProperty("singleEvents", String.valueOf(true));
                conn.setRequestProperty("orderBy", "startTime");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setInstanceFollowRedirects(true);

                String eventInfo = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String tmp = "";
                while((tmp = in.readLine()) != null) {
                    eventInfo += tmp;
                }
                in.close();

                Log.d("Google Calendar", "Event info: " + eventInfo);

                JSONObject json = new JSONObject(eventInfo);
                JSONArray events = json.getJSONArray("items");

                for(int i = events.length() - 1; i > events.length() - 5 ; i--) {
                    JSONObject event = events.getJSONObject(i);
                    JSONObject start = event.getJSONObject("start");
                    JSONObject end = event.getJSONObject("end");

                    if(withinTimeWindow(start, end, now.toString())) {
                        currentlyBusyNotif();
                    }
                    Log.e("Google Calendar", start.toString() + " " + end.toString());
                }

            } catch (IOException ioEx) {
                Log.e("Google Calendar", "Network error when querying userinfo endpoint", ioEx);
            } catch (JSONException jsonEx) {
                Log.e("Google Calendar", "Failed to parse userinfo response");
            }
        });

    }

    private void currentlyBusyNotif() {

        Intent intent = new Intent(GoogleCalendarAccess.this, ReminderBroadcast.class);
        intent.putExtra("type", "Calendar");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(GoogleCalendarAccess.this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long timeAtButtonClick = System.currentTimeMillis();

        //change this variable to whatever delay you want
        long delaySecondsInMillis = 2000;

        Log.d("Google Calendar", "Notification queued");

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                timeAtButtonClick + delaySecondsInMillis, pendingIntent);
        return;
    }

    private boolean withinTimeWindow(JSONObject start, JSONObject end, String toString) {
        return true;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuthService.dispose();
        mExecutor.shutdownNow();
    }


}