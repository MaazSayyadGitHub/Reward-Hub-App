package com.maaz.rewardhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import com.maaz.rewardhub.databinding.ActivityWatchAndEarnBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WatchAndEarn extends AppCompatActivity implements MaxAdListener {

    private MaxAdView watchAndEarnmRecAds;
    private MaxInterstitialAd interstitialAd;

    ActivityWatchAndEarnBinding binding;
    private boolean flag = false;
    ProgressDialog progressDialog;

    FirebaseFirestore database;
    private int playCounts;
    private SharedPreferences preferences;

    FirebaseDatabase realTimeDatabase;
    String formattedDate; // for server Date

    private long adDisplayedTime = 0;
    private boolean adClicked = false;
    private boolean isAppInstalledFlag = false;

    private boolean flag1 = true;
    private boolean flag2 = false;
    private boolean flag3 = false;
    private boolean flag4 = false;
    private boolean flag5 = false;
    private boolean flag6 = false;
    private boolean flag7 = false;
    private boolean flag8 = false;
    private boolean flag9 = false;
    private boolean flag10 = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWatchAndEarnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        realTimeDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Ads..");
        progressDialog.setCancelable(false);

        GetServerDate();

        // sharedPreference for daily limit.
        preferences = getSharedPreferences("DailyWatchLimit", MODE_PRIVATE);

        // lastScratch date.
        String lastScratchDate = preferences.getString("lastWatch", "");

        // load banner MRec ads
        watchAndEarnmRecAds = findViewById(R.id.mRecAds);
        watchAndEarnmRecAds.loadAd();
        watchAndEarnmRecAds.startAutoRefresh();

        // AlertDialog for Successfully coins gain.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Watch Ad to Earn Coins.")
                .setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // increment daily limit in sharedPreferences.

                // Check if the user has already done captcha today.
                if (lastScratchDate.equals(formattedDate)) { // calendar.get(Calendar.DATE)
                    // Get the captcha count.
                    playCounts = preferences.getInt("watchCount", 0); //1 2 3 4

                    // Check if the user has reached the daily limit.
                    if (playCounts >= 10) {
                        Toast.makeText(WatchAndEarn.this, "You have reached the daily limit", Toast.LENGTH_LONG).show();
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

                        if (playCounts == 4) {

                            // don't increment playCount

                        } else {
                            // Increment the captcha count and save it.
                            playCounts++;
                            preferences.edit().putInt("watchCount", playCounts).apply();
                        }
                    }
                } else {
                    // normal code for captcha after right submit.
                    // Toast.makeText(Recaptcha.this, "Right Captcha", Toast.LENGTH_SHORT).show();
                    // refreshCaptcha();


                    // Reset the scratch count to 1 and save the current date.
                    playCounts = 1;
                    preferences.edit().putString("lastWatch", formattedDate).apply(); // calendar.get(Calendar.DATE)
                    preferences.edit().putInt("watchCount", playCounts).apply();
                }

                Toast.makeText(WatchAndEarn.this, playCounts + " Watch Done", Toast.LENGTH_LONG).show();

                // here only first time activity will be finish
                if (playCounts == 1) {
                    // finish Recaptcha Activity
                    finish();
                }

            }
        });
        AlertDialog alert = builder.create();

        // preferences.getInt("scratchCount", 0) we are getting counts from sharedPreferences.

        if (preferences.getInt("watchCount", 0) >= 1) {
            binding.btn1.setVisibility(View.INVISIBLE);
            flag2 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 2) {
            binding.btn2.setVisibility(View.INVISIBLE);
            flag3 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 3) {
            binding.btn3.setVisibility(View.INVISIBLE);
            flag4 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 4) {
            binding.btn4.setVisibility(View.INVISIBLE);
            flag5 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 5) {
            binding.btn5.setVisibility(View.INVISIBLE);
            flag6 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 6) {
            binding.btn6.setVisibility(View.INVISIBLE);
            flag7 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 7) {
            binding.btn7.setVisibility(View.INVISIBLE);
            flag8 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 8) {
            binding.btn8.setVisibility(View.INVISIBLE);
            flag9 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 9) {
            binding.btn9.setVisibility(View.INVISIBLE);
            flag10 = true;
        }
        if (preferences.getInt("watchCount", 0) >= 10) {
            binding.btn10.setVisibility(View.INVISIBLE);
            binding.finishMsg.setVisibility(View.VISIBLE);
        }

        binding.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag1) {
                    alert.show();
                }
            }
        });

        binding.btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag2) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag3) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag4) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }

            }
        });

        binding.btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag5) {
                    flag = true; // for install app toast.
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag6) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag7) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag8) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag9) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btn10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag10) {
                    alert.show();

                } else {
                    Toast.makeText(WatchAndEarn.this, "Click On Previous", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void addToDatabase() {
        long captchaCoins = 5; // storing captcha amount in this variable.
        database.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                // here the long cash will be added to existing value.
                .update("coins", FieldValue.increment(captchaCoins))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(WatchAndEarn.this, captchaCoins + " Coins Added Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void addToDatabase5thBtn() {
        long captchaCoins = 10; // storing captcha amount in this variable.
        database.collection("User")
                .document(FirebaseAuth.getInstance().getUid())
                // here the long cash will be added to existing value.
                .update("coins", FieldValue.increment(captchaCoins))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(WatchAndEarn.this, captchaCoins + " Coins Added Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void GetServerDate() {
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
                Toast.makeText(WatchAndEarn.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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

        String lastScratchDate = preferences.getString("lastWatch", "");
        if (!lastScratchDate.equals(RefreshServerDate)) {  // calendar.get(Calendar.DATE)
            preferences.edit().putString("lastWatch", RefreshServerDate).apply(); // calendar.get(Calendar.DATE)
            preferences.edit().putInt("watchCount", 0).apply();
        }
    }


    private void loadIntersAds() {
        interstitialAd = new MaxInterstitialAd("f7800f281a0f3aa4", this);
        interstitialAd.setListener(this);

        // Load the first ad
        interstitialAd.loadAd();
    }

    @Override
    public void onAdLoaded(MaxAd maxAd) {
        progressDialog.dismiss();
        interstitialAd.showAd();
    }

    @Override
    public void onAdDisplayed(MaxAd maxAd) {
        if (flag) {
            Toast.makeText(this, "Click on Ad and Install App", Toast.LENGTH_LONG).show();

            // Record the current time when the ad is displayed
            adDisplayedTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onAdHidden(MaxAd maxAd) {

        if (flag) {
            // Check if the adClicked flag is true and if the elapsed time since the ad was displayed is greater than 1 minute
            if (adClicked && System.currentTimeMillis() - adDisplayedTime > 150000) {
                // Give coins to the user

                // add 10 coins on 5th button
                addToDatabase5thBtn();

                // increment playCount for 5th watch here. if user installed App
                playCounts++;
                preferences.edit().putInt("watchCount", playCounts).apply();

                Toast.makeText(WatchAndEarn.this, playCounts + " Watch Done", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "You Haven't Installed App", Toast.LENGTH_LONG).show();
            }

            flag = false; // for not show this stuffs on other buttons.

            // finish Activity
            finish();

        } else {
            // add coins for other buttons
            addToDatabase();
            finish();
        }

    }

    @Override
    public void onAdClicked(MaxAd maxAd) {
        // Set the adClicked flag to true when the user clicks on the ad
        adClicked = true;
    }

    @Override
    public void onAdLoadFailed(String s, MaxError maxError) {
        Toast.makeText(this, "ad not loaded" + maxError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
        Toast.makeText(this, "ad failed to display" + maxError.getMessage(), Toast.LENGTH_SHORT).show();
    }
}