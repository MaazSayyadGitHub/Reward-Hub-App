package com.maaz.rewardhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
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
import com.maaz.rewardhub.databinding.ActivityRecaptchaBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Recaptcha extends AppCompatActivity implements MaxAdListener {

    ActivityRecaptchaBinding binding;

    public int[] scratchCoins = {2, 4, 6, 8, 10};
    public int scratchGeneratedAmount;

    String[] characters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "@", "!", "$", "&", "(", ")", "-"};
    String generatedCaptcha = "";

    private int playCounts;
    private SharedPreferences preferences;

//    WithdrawRequest withdrawRequest;
//    private Date serverDate;

    FirebaseFirestore database;

    FirebaseDatabase realTimeDatabase;
    String formattedDate;

    private MaxAdView RecaptchamRecAds; // mRec
    private MaxInterstitialAd interstitialAd; // interstitial
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecaptchaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // load banner mRec ad
        RecaptchamRecAds = binding.mRecAds;
        RecaptchamRecAds.loadAd();
        RecaptchamRecAds.startAutoRefresh();

        database = FirebaseFirestore.getInstance();
        realTimeDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Ads");
        progressDialog.setCancelable(false);

        refreshCaptcha();

        // this will generate random coins for user.
        scratchAmount();

        GetServerDate();

        // AlertDialog for Successfully coins gain.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Watch Ad to Earn Coins.")
                .setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // load ads here
                loadIntersAds();
                progressDialog.show();

                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();


        // code for daily limit.
        // server time.
        // WithdrawRequest withdrawRequest = new WithdrawRequest();
        // serverDate = withdrawRequest.getCreatedAt();

        // sharedPreference for daily limit.
        preferences = getSharedPreferences("DailyCaptchaLimit", MODE_PRIVATE);

        // lastCaptcha date.
        String lastCaptchaDate = preferences.getString("lastCaptcha", "");

        // default value for each day.
        // playCounts = preferences.getInt("playCount", 0);

        // Local Date for daily check - but we are using server time.
        // Calendar calendar = Calendar.getInstance();


        // refreshing captcha on pressing refresh button.
        binding.generateCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCaptcha();
            }
        });

        // validating captcha and user input.
        binding.submitCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = binding.captchaInput.getText().toString().trim();

                if (validateCaptcha(userInput)){
                    // increment daily limit in sharedPreferences.

                    // Check if the user has already done captcha today.
                    if (lastCaptchaDate.equals(formattedDate)) { // calendar.get(Calendar.DATE)
                        // Get the captcha count.
                        playCounts = preferences.getInt("captchaCount", 0);

                        // Check if the user has reached the daily limit.
                        if (playCounts >= 5) {
                            Toast.makeText(Recaptcha.this, "You have reached the daily limit", Toast.LENGTH_LONG).show();
                            finish();
                        } else {

                            // normal code for captcha after right submit.
                            Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                            refreshCaptcha();

                            // add coins to database after showing dialog.
                            alert.show();

                            // Increment the captcha count and save it.
                            playCounts++;
                            preferences.edit().putInt("captchaCount", playCounts).apply();
                        }
                    } else {
                        // normal code for captcha after right submit.
                        Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                        refreshCaptcha();

                        // Reset the scratch count to 1 and save the current date.
                        playCounts = 1;
                        preferences.edit().putString("lastCaptcha", formattedDate).apply(); // calendar.get(Calendar.DATE)
                        preferences.edit().putInt("captchaCount", playCounts).apply();
                    }

                    Toast.makeText(Recaptcha.this, playCounts + " Captcha Done", Toast.LENGTH_LONG).show();

                    // here only first time activity will be finish
                    if (playCounts == 1){
                        // finish Recaptcha Activity
                        finish();
                    }

                } else {
                    Toast.makeText(Recaptcha.this, "Wrong Captcha", Toast.LENGTH_SHORT).show();
                }
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
                Toast.makeText(Recaptcha.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Reset the captcha count at midnight.
        // Calendar calendar = Calendar.getInstance();
        // WithdrawRequest wt = new WithdrawRequest();
        // Date RefreshServerDate = wt.getCreatedAt();

        String RefreshServerDate = formattedDate;

        String lastCaptchaDate = preferences.getString("lastCaptcha", "");
        if (!lastCaptchaDate.equals(RefreshServerDate)) {  // calendar.get(Calendar.DATE)
            preferences.edit().putString("lastCaptcha", RefreshServerDate).apply(); // calendar.get(Calendar.DATE)
            preferences.edit().putInt("captchaCount", 0).apply();
        }
    }

    public void scratchAmount(){
        Random random = new Random();
        scratchGeneratedAmount = scratchCoins[random.nextInt(scratchCoins.length)];
    }

    public void addToDatabase(){

        // long captchaCoins = 10; // storing captcha amount in this variable.
        database.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                // here the long cash will be added to existing value.
                .update("coins", FieldValue.increment(scratchGeneratedAmount))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Recaptcha.this, scratchGeneratedAmount + " Coins Added Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateCaptcha(String userInput){
        String OrgCaptcha = binding.captchaTxt.getText().toString().trim(); // generated captcha
        return userInput.equalsIgnoreCase(OrgCaptcha); // user input
    }


    private void generateCaptcha() {
        Random random = new Random();
        generatedCaptcha = "";
        for (int i = 0; i < 6; i++){
            generatedCaptcha += characters[random.nextInt(characters.length)]; // here we are giving index of char array to pick one. 6 times.
        }
    }

    private void refreshCaptcha() {
        // get captcha generated here
        generateCaptcha();
        String Captcha = generatedCaptcha;
        binding.captchaTxt.setText(Captcha); // set captcha
        binding.captchaInput.setText(""); // clear input field
    }


    // ADS SECTION

    private void loadIntersAds(){
        interstitialAd = new MaxInterstitialAd( "f7800f281a0f3aa4", this );
        interstitialAd.setListener(this);

        // Load the first ad
        interstitialAd.loadAd();
    }


    // maxAdListener
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
        addToDatabase();
        // finish Recaptcha Activity
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