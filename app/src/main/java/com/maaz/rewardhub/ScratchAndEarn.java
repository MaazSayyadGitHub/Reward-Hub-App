package com.maaz.rewardhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
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
import com.maaz.rewardhub.databinding.ActivityMainBinding;
import com.maaz.rewardhub.databinding.ActivityScratchAndEarnBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import dev.skymansandy.scratchcardlayout.listener.ScratchListener;
import dev.skymansandy.scratchcardlayout.ui.ScratchCardLayout;

public class ScratchAndEarn extends AppCompatActivity implements MaxAdListener {

    ActivityScratchAndEarnBinding binding;
    FirebaseFirestore database;

    public int[] scratchCoins = {5, 7, 8, 10, 12, 15};
    public int scratchGeneratedAmount;

    private int playCounts;
    private SharedPreferences preferences;

//    private Date serverDate;

    FirebaseDatabase realTimeDatabase;
    String formattedDate;

    private MaxAdView ScratchAndEarnMRecAds;
    private MaxInterstitialAd interstitialAd; // interstitial
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScratchAndEarnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // load banner mRec ad
        ScratchAndEarnMRecAds = binding.mRecAds;
        ScratchAndEarnMRecAds.loadAd();
        ScratchAndEarnMRecAds.startAutoRefresh();

        database = FirebaseFirestore.getInstance();
        realTimeDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Ads..");
        progressDialog.setCancelable(false);

        // get current date
        GetServerDate();

        // this will generate random scratch amount.
        scratchAmount();

        // Here code for daily limit.

        // server time.
        // WithdrawRequest withdrawRequest = new WithdrawRequest();
        // serverDate = withdrawRequest.getCreatedAt();

        // sharedPreference for daily limit.
        preferences = getSharedPreferences("DailyScratchLimit", MODE_PRIVATE);

        // lastScratch date.
        String lastScratchDate = preferences.getString("lastScratch", "");


        // Alert Dialog for showing Coins won.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Watch Ad to Earn "+ scratchGeneratedAmount + " Coins")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
//                        Toast.makeText(ScratchAndEarn.this, "Scratch Complete", Toast.LENGTH_SHORT).show();


                        // increment daily limit in sharedPreferences.

                        // Check if the user has already done captcha today.
                        if (lastScratchDate.equals(formattedDate)) { // calendar.get(Calendar.DATE)
                            // Get the captcha count.
                            playCounts = preferences.getInt("scratchCount", 0);

                            // Check if the user has reached the daily limit.
                            if (playCounts >= 5) {
                                Toast.makeText(ScratchAndEarn.this, "You have reached the daily limit", Toast.LENGTH_LONG).show();
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

//                                // add coins.
//                                addToDatabase();

                                // Increment the captcha count and save it.
                                playCounts++;
                                preferences.edit().putInt("scratchCount", playCounts).apply();
                            }
                        } else {
                            // normal code for captcha after right submit.
                            // Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                            // refreshCaptcha();


                            // Reset the scratch count to 1 and save the current date.
                            playCounts = 1;
                            preferences.edit().putString("lastScratch", formattedDate).apply(); // calendar.get(Calendar.DATE)
                            preferences.edit().putInt("scratchCount", playCounts).apply();
                        }

                        Toast.makeText(ScratchAndEarn.this, playCounts + " Scratch Done", Toast.LENGTH_LONG).show();

                        // here only first time activity will be finish
                        if (playCounts == 1){
                            // finish Recaptcha Activity
                            finish();
                        }
                    }
                });
        AlertDialog alert = builder.create();
        // alert.show();


        // scratch listener
        binding.scratchCard.setScratchListener(new ScratchListener() {
            @Override
            public void onScratchStarted() {
                Toast.makeText(ScratchAndEarn.this, "Scratch Started", Toast.LENGTH_SHORT).show();
                binding.scratchCoins.setText(String.valueOf(scratchGeneratedAmount));
            }

            @Override
            public void onScratchProgress(@NonNull ScratchCardLayout scratchCardLayout, int i) {
                if (i >= 75){
                    // show alert dialog.
                    alert.show();
                }
            }

            @Override
            public void onScratchComplete() {

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
                Toast.makeText(ScratchAndEarn.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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

        String lastScratchDate = preferences.getString("lastScratch", "");
        if (!lastScratchDate.equals(RefreshServerDate)) {  // calendar.get(Calendar.DATE)
            preferences.edit().putString("lastScratch", RefreshServerDate).apply(); // calendar.get(Calendar.DATE)
            preferences.edit().putInt("scratchCount", 0).apply();
        }
    }

    public void addToDatabase(){

//        long scratchCoins = 5; // storing scratch amount in this variable for toast.
        database.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                // here the long cash will be added to existing value.
                .update("coins", FieldValue.increment(scratchGeneratedAmount))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ScratchAndEarn.this, scratchGeneratedAmount + " Coins Added Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void scratchAmount(){
        Random random = new Random();
        scratchGeneratedAmount = scratchCoins[random.nextInt(scratchCoins.length)];
    }

    // ADS SECTION

    private void loadIntersAds(){
        interstitialAd = new MaxInterstitialAd( "f7800f281a0f3aa4", this );
        interstitialAd.setListener(this);

        // Load the first ad
        interstitialAd.loadAd();
    }

    @Override
    public void onAdLoaded(MaxAd maxAd) {
        progressDialog.dismiss();
        // show ad when it is loaded
        interstitialAd.showAd();
    }

    @Override
    public void onAdDisplayed(MaxAd maxAd) {

    }

    @Override
    public void onAdHidden(MaxAd maxAd) {
        // give coins to user after watching ads.
        addToDatabase();

        // finish Scratch Activity
        finish();
    }

    @Override
    public void onAdClicked(MaxAd maxAd) {

    }

    @Override
    public void onAdLoadFailed(String s, MaxError maxError) {
        Toast.makeText(this, "AD FAILED TO LOAD", Toast.LENGTH_LONG).show();
        interstitialAd.loadAd();
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
        Toast.makeText(this, "AD FAILED TO DISPLAY", Toast.LENGTH_LONG).show();
    }
}