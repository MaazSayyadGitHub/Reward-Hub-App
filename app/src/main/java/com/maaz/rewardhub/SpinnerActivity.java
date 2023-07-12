package com.maaz.rewardhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import com.maaz.rewardhub.SpinWheel.LuckyWheelView;
import com.maaz.rewardhub.SpinWheel.model.LuckyItem;
import com.maaz.rewardhub.databinding.ActivitySpinnerBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SpinnerActivity extends AppCompatActivity implements MaxAdListener {

    ActivitySpinnerBinding binding;
    long cash;

    private int playCounts;
    private SharedPreferences preferences;
    String lastSpinDate;

    FirebaseDatabase realTimeDatabase;
    String formattedDate;

    private MaxAdView spinBannerAdview;
    private MaxInterstitialAd interstitialAd; // interstitial
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpinnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //load ads
        spinBannerAdview = binding.adView;
        spinBannerAdview.loadAd();
        spinBannerAdview.startAutoRefresh();

        List<LuckyItem> luckyItemsData = new ArrayList<>();

        // creating wheel items
        LuckyItem luckyItem1 = new LuckyItem();
        luckyItem1.topText = "3";
        luckyItem1.secondaryText = "COINS";
        luckyItem1.textColor = Color.parseColor("#212121");
        luckyItem1.color = Color.parseColor("#eceff1");
        luckyItemsData.add(luckyItem1);

        LuckyItem luckyItem2 = new LuckyItem();
        luckyItem2.topText = "5";
        luckyItem2.secondaryText = "COINS";
        luckyItem2.color = Color.parseColor("#00cf00");
        luckyItem2.textColor = Color.parseColor("#ffffff");
        luckyItemsData.add(luckyItem2);

        LuckyItem luckyItem3 = new LuckyItem();
        luckyItem3.topText = "6";
        luckyItem3.secondaryText = "COINS";
        luckyItem3.textColor = Color.parseColor("#212121");
        luckyItem3.color = Color.parseColor("#eceff1");
        luckyItemsData.add(luckyItem3);

        LuckyItem luckyItem4 = new LuckyItem();
        luckyItem4.topText = "8";
        luckyItem4.secondaryText = "COINS";
        luckyItem4.color = Color.parseColor("#7f00d9");
        luckyItem4.textColor = Color.parseColor("#ffffff");
        luckyItemsData.add(luckyItem4);

        LuckyItem luckyItem5 = new LuckyItem();
        luckyItem5.topText = "10";
        luckyItem5.secondaryText = "COINS";
        luckyItem5.textColor = Color.parseColor("#212121");
        luckyItem5.color = Color.parseColor("#eceff1");
        luckyItemsData.add(luckyItem5);

        LuckyItem luckyItem6 = new LuckyItem();
        luckyItem6.topText = "12";
        luckyItem6.secondaryText = "COINS";
        luckyItem6.color = Color.parseColor("#dc0000");
        luckyItem6.textColor = Color.parseColor("#ffffff");
        luckyItemsData.add(luckyItem6);

        LuckyItem luckyItem7 = new LuckyItem();
        luckyItem7.topText = "15";
        luckyItem7.secondaryText = "COINS";
        luckyItem7.textColor = Color.parseColor("#212121");
        luckyItem7.color = Color.parseColor("#eceff1");
        luckyItemsData.add(luckyItem7);

        LuckyItem luckyItem8 = new LuckyItem();
        luckyItem8.topText = "0";
        luckyItem8.secondaryText = "COINS";
        luckyItem8.color = Color.parseColor("#008bff");
        luckyItem8.textColor = Color.parseColor("#ffffff");
        luckyItemsData.add(luckyItem8);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Ads");
        progressDialog.setCancelable(false);

        // set items data on wheelView
        binding.wheelview.setData(luckyItemsData);
        // how much time we want to rotate.
        binding.wheelview.setRound(5);

        // spin the wheel
        binding.spinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we need to get first any random number, till 8, becos items are 8.
                Random r = new Random();
                int randomNumber = r.nextInt(8);
                binding.wheelview.startLuckyWheelWithTargetIndex(randomNumber); // will lie on any random item in wheel.
            }
        });


        realTimeDatabase = FirebaseDatabase.getInstance();

        // sharedPreference for daily limit.
        preferences = getSharedPreferences("DailySpinLimit", MODE_PRIVATE);

        // lastScratch date.
        lastSpinDate = preferences.getString("lastSpin", "");

        GetServerDate();

        // set on itemSelected.
        binding.wheelview.setLuckyRoundItemSelectedListener(new LuckyWheelView.LuckyRoundItemSelectedListener() {
            @Override
            public void LuckyRoundItemSelected(int index) {
                updateSpinCoins(index);
            }
        });

    }

    // update Spin Coin
    public void updateSpinCoins(int index){
        cash = 0;
        switch (index){
            case 0:
                cash = 3;
                break;
            case 1:
                cash = 5;
                break;
            case 2:
                cash = 6;
                break;
            case 3:
                cash = 8;
                break;
            case 4:
                cash = 10;
                break;
            case 5:
                cash = 12;
                break;
            case 6:
                cash = 15;
                break;
            case 7:
                cash = 0;
                break;
        }



        // Alert Dialog for showing Coins won.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Watch Ads to Earn "+ cash + " Coins")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
//                        Toast.makeText(ScratchAndEarn.this, "Scratch Complete", Toast.LENGTH_SHORT).show();


                        // increment daily limit in sharedPreferences.

                        // Check if the user has already done captcha today.
                        if (lastSpinDate.equals(formattedDate)) { // calendar.get(Calendar.DATE)
                            // Get the captcha count.
                            playCounts = preferences.getInt("spinCount", 0);

                            // Check if the user has reached the daily limit.
                            if (playCounts >= 5) {
                                Toast.makeText(SpinnerActivity.this, "You have reached the daily limit", Toast.LENGTH_LONG).show();
                                finish();
                            } else {

                                // normal code for captcha after right submit.
                                // Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                                // refreshCaptcha();

                                // add coins to database after showing dialog.
                                // alert.show();

                                // laod ads here
                                loadIntersAds();
                                progressDialog.show();


                                // Increment the captcha count and save it.
                                playCounts++;
                                preferences.edit().putInt("spinCount", playCounts).apply();
                            }
                        } else {
                            // normal code for captcha after right submit.
                            // Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                            // refreshCaptcha();


                            // Reset the scratch count to 1 and save the current date.
                            playCounts = 1;
                            preferences.edit().putString("lastSpin", formattedDate).apply(); // calendar.get(Calendar.DATE)
                            preferences.edit().putInt("spinCount", playCounts).apply();
                        }

                        Toast.makeText(SpinnerActivity.this, playCounts + " Spin Done", Toast.LENGTH_LONG).show();

                        // here only first time activity will be finish
                        if (playCounts == 1){
                            // finish Recaptcha Activity
                            finish();
                        }

                    }
                });
        AlertDialog alert = builder.create();
        // show dialog box here
        alert.show();


    }

    public void addToDatabase(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        long addedCoins = cash; // storing cash in this variable for toast.
        database.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                // here the long cash will be added to existing value.
                .update("coins", FieldValue.increment(cash))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SpinnerActivity.this, addedCoins +" Coins Added", Toast.LENGTH_LONG).show();
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
                Toast.makeText(SpinnerActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Reset the spin count at midnight.
        // Calendar calendar = Calendar.getInstance();

        // get new Date
        String RefreshServerDate = formattedDate;

        String lastScratchDate = preferences.getString("lastSpin", "");
        if (!lastScratchDate.equals(RefreshServerDate)) {  // calendar.get(Calendar.DATE)
            preferences.edit().putString("lastSpin", RefreshServerDate).apply(); // calendar.get(Calendar.DATE)
            preferences.edit().putInt("spinCount", 0).apply();
        }
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
        // add coins.
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