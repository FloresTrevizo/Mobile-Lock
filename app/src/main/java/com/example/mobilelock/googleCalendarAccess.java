package com.example.mobilelock;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

//import com.google.android.gms.auth.api.identity.SaveAccountLinkingTokenRequest;

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

public class googleCalendarAccess extends AppCompatActivity {
    private Button button;
    private static final int RC_AUTH = 100;
    private AuthorizationService mAuthService;
    private AuthorizationServiceConfiguration mAuthServiceConfig;
    private AuthStateManager mAuthStateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_uri_receiver);

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
                getString(R.string.SCOPE_DRIVE),
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
                                mAuthStateManager.updateAfterTokenResponse(response, ex);
                                //token exchange succeeded
                            }
                        }
                    });
            }
        }
        else if (resultCode == RESULT_CANCELED) {

        }
        if(mAuthStateManager.getCurrent().isAuthorized()) {
            Log.d("Google Calendar", "SUCCESS");
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

        /*Configuration mConfiguration = Configuration.getInstance(this);

        Uri userInfoEndpoint =
                mConfiguration.getUserInfoEndpointUri() != null
                        ? Uri.parse(mConfiguration.getUserInfoEndpointUri().toString())
                        : Uri.parse(discovery.getUserinfoEndpoint().toString());*/

    }

}