package com.maaz.rewardhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

public class SplashScreen extends AppCompatActivity {

    boolean isAdLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Applovin Initialization
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance( SplashScreen.this ).setMediationProvider( "max" );
        AppLovinSdk.initializeSdk( SplashScreen.this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
            {
                // AppLovin SDK is initialized, start loading ads
                isAdLoaded = true;
            }
        } );

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this, SignInActivity.class));
                finish();

//                Toast.makeText(SplashScreen.this, "Ads Loaded", Toast.LENGTH_SHORT).show();

            }
        }, 3000);
    }
}