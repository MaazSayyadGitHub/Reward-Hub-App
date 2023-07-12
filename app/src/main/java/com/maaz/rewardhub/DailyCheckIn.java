package com.maaz.rewardhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maaz.rewardhub.databinding.ActivityDailyCheckInBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyCheckIn extends AppCompatActivity implements MaxAdListener {

    private MaxAdView dailyCheckInmRecAds;
    private MaxInterstitialAd interstitialAd;

    ActivityDailyCheckInBinding binding;
    private boolean isAdShown = false;

    private long adDisplayedTime = 0;
    private boolean adClicked = false;
//    public static boolean isAppInstalledFlag = false;

    ProgressDialog progressDialog;

    FirebaseFirestore database;
    private int playCounts;
    private SharedPreferences preferences;
    private SharedPreferences checkPreferences;

    FirebaseDatabase realTimeDatabase;
    String formattedDate; // for server Date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDailyCheckInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // showing mRec Ads
        dailyCheckInmRecAds = binding.mRecAds;
        dailyCheckInmRecAds.loadAd();
        dailyCheckInmRecAds.startAutoRefresh();

        database = FirebaseFirestore.getInstance();
        realTimeDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Ads..");
        progressDialog.setCancelable(false);

        GetServerDate();

        // code for user checkedIn or not for withdraw
        checkPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);

        // sharedPreference for daily limit.
        preferences = getSharedPreferences("DailyCheckLimit", MODE_PRIVATE);

        // lastScratch date.
        String lastDailyCheckDate = preferences.getString("lastCheck", "");

        // AlertDialog for Successfully coins gain.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Install App to Earn 200 Coins.")
                .setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // increment daily limit in sharedPreferences.

                // Check if the user has already done captcha today.
                if (lastDailyCheckDate.equals(formattedDate)) { // calendar.get(Calendar.DATE)
                    // Get the captcha count.
                    playCounts = preferences.getInt("checkCount", 0);

                    // Check if the user has reached the daily limit.
                    if (playCounts >= 2) {
                        Toast.makeText(DailyCheckIn.this, "You have Already Earned Coins.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {

                        // normal code for captcha after right submit.
                        // Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                        // refreshCaptcha();

                        // add coins to database after showing dialog.
                        // alert.show();

                        // show ads first.
                        loadIntersAds();
                        progressDialog.show();


                        // don't increment here

                        // Increment the captcha count and save it.
                        // playCounts++;
                        // preferences.edit().putInt("checkCount", playCounts).apply();
                    }
                } else {
                    // normal code for captcha after right submit.
                    // Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                    // refreshCaptcha();


                    // Reset the scratch count to 1 and save the current date.
                    playCounts = 1;
                    preferences.edit().putString("lastCheck", formattedDate).apply(); // calendar.get(Calendar.DATE)
                    preferences.edit().putInt("checkCount", playCounts).apply();
                }


                // here only first time activity will be finish
                if (playCounts == 1){
                     Toast.makeText(DailyCheckIn.this, "If Ads not Load Do Again.", Toast.LENGTH_LONG).show();
                    // finish Recaptcha Activity
                    finish();
                }

            }
        });
        AlertDialog alert = builder.create();

        binding.checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });

    }


    public void addToDatabase(){

        long captchaCoins = 200; // storing captcha amount in this variable.
        database.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                // here the long cash will be added to existing value.
                .update("coins", FieldValue.increment(captchaCoins))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(DailyCheckIn.this, captchaCoins + " Coins Added Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void GetServerDate(){
        DatabaseReference timestampRef = realTimeDatabase.getReference("timestamp");

        // Use the ServerValue.TIMESTAMP constant to write the current server timestamp to the database
        timestampRef.setValue(ServerValue.TIMESTAMP);

        timestampRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the timestamp value from the snapshot
                long timestamp = snapshot.getValue(Long.class);

                // Convert the timestamp to a Date object
                Date date = new Date(timestamp);

                // Format the date as desired
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                formattedDate = dateFormat.format(date);

                // Do something with the formatted date object, such as display it in a TextView
                // binding.currentDate.setText(formattedDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyCheckIn.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Reset the scratch count at midnight.
        // Calendar calendar = Calendar.getInstance();

        // get new Date
        String RefreshServerDate = formattedDate;

        String lastScratchDate = preferences.getString("lastCheck", "");
        if (!lastScratchDate.equals(RefreshServerDate)) {  // calendar.get(Calendar.DATE) // if date change
            preferences.edit().putString("lastCheck", RefreshServerDate).apply(); // calendar.get(Calendar.DATE)
            preferences.edit().putInt("checkCount", 0).apply();
            // set false for newly daily check in
            checkPreferences.edit().putBoolean("hasCheckedInToday", false);
        }
    }


    private void loadIntersAds(){
        interstitialAd = new MaxInterstitialAd( "f7800f281a0f3aa4", this );
        interstitialAd.setListener(this);

        // Load the first ad
        interstitialAd.loadAd();
    }

    @Override
    public void onAdLoaded(MaxAd maxAd) {
    // it will be contineusly loading ads one after another.
    // if its first time then load otherwise don't load.

        progressDialog.dismiss();
        interstitialAd.showAd();

    }

    @Override
    public void onAdDisplayed(MaxAd maxAd) {
        // Record the current time when the ad is displayed
        adDisplayedTime = System.currentTimeMillis();

        Toast.makeText(this, "Click On Ad And Install App.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAdHidden(MaxAd maxAd) {
        // Check if the adClicked flag is true and if the elapsed time since the ad was displayed is greater than 1 minute
        if (adClicked && System.currentTimeMillis() - adDisplayedTime > 150000) {
            // Give coins to the user

            // 200 coins
            addToDatabase();

            // isAppInstalledFlag = true;
            // store true in sharedPreferences for withdraw that user has completed daily check in
            SharedPreferences.Editor editor = checkPreferences.edit();
            editor.putBoolean("hasCheckedInToday", true);
            editor.apply();

            // Increment the captcha count and save it.
            playCounts++;
            preferences.edit().putInt("checkCount", playCounts).apply();
        } else {
            Toast.makeText(this, "You Haven't Installed App", Toast.LENGTH_LONG).show();
        }

        // Reset the adClicked flag and adDisplayedTime for the next ad
        adClicked = false;
        adDisplayedTime = 0;
    }

    @Override
    public void onAdClicked(MaxAd maxAd) {
        // Set the adClicked flag to true when the user clicks on the ad
        adClicked = true;
    }

    @Override
    public void onAdLoadFailed(String s, MaxError maxError) {
        Toast.makeText(this, "ad not loaded" +maxError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
        Toast.makeText(this, "ad failed to display" + maxError.getMessage(), Toast.LENGTH_SHORT).show();
    }

}