package com.maaz.rewardhub;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.applovin.mediation.ads.MaxAdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maaz.rewardhub.databinding.ActivityMainBinding;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity{

    ActivityMainBinding binding;
    FirebaseFirestore database;
    FirebaseAuth auth;

    private MaxAdView homeBannerAdview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // load banner ads
        homeBannerAdview = binding.adView;
        homeBannerAdview.loadAd();
        homeBannerAdview.startAutoRefresh();

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // set custom toolbar
        setSupportActionBar(binding.toolBar);

        // logOut button
//        binding.logOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//
//                Toast.makeText(MainActivity.this, "LogOut Successfully", Toast.LENGTH_LONG).show();
//
//                Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
//                startActivity(loginIntent);
//            }
//        });

        database.collection("User")
                .document(auth.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class); // convert that all snapshot to user model structure object.
                        binding.toolbarCoins.setText(String.valueOf(user.getCoins()));
                    }
                });

        // load Home Fragment first.
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, new HomeFragment());
        ft.commit();

        // set Bottom Navigation Bar
        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                FragmentTransaction ft = fm.beginTransaction(); // we should initialize transaction here.
                switch (i){
                    case 0 :
                        ft.replace(R.id.content, new HomeFragment());
                        ft.commit();
                        break;
                    case 1 :
                        ft.replace(R.id.content, new InviteFragment());
                        ft.commit();
                        break;
                    case 2 :
                        ft.replace(R.id.content, new WalletFragment());
                        ft.commit();
                        break;
//                    case 3 :
//                        ft.replace(R.id.content, new ProfileFragment());
//                        ft.commit();
//                        break;
                }
                return false;
            }
        });

    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Exit!");
//        builder.setMessage("Do you want to Exit!");
//        builder.setIcon(R.drawable.exit);
//        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                MainActivity.super.onBackPressed(); // we are now into Dialog Class thats why MainActivity.
//            }
//        });
//        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
//            }
//        });
////        builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
////            @Override
////            public void onClick(DialogInterface dialogInterface, int i) {
////                Toast.makeText(MainActivity.this, "canceled", Toast.LENGTH_SHORT).show();
////            }
////        });
//        builder.show();
//    }
}