package com.maaz.rewardhub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maaz.rewardhub.databinding.FragmentWalletBinding;

import java.util.ArrayList;


public class WalletFragment extends Fragment {


    public WalletFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentWalletBinding binding;
    FirebaseFirestore database;
    FirebaseAuth auth;
    User user;

    SharedPreferences checkPreferences;

    // private MaxAdView WalletFragmentAdview;

    private ArrayList<spinnerItems> spinnerList;
    private ArrayAdapter<spinnerItems> spinnerAdapter;
    long amount; // spinner amount

//    private boolean checkAppInstallFlag = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);

        // WalletFragmentAdview = binding.adView;
        // WalletFragmentAdview.loadAd();

//        checkAppInstallFlag = DailyCheckIn.isAppInstalledFlag;

        // this is for check if user has daily check-in or not
        checkPreferences = getActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        // spinner items codes here.
        spinnerItemList();
        spinnerAdapter = new SpinnerAdapter(getContext(), spinnerList);
        binding.spinner.setAdapter(spinnerAdapter);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerItems spinnerItems = (spinnerItems) parent.getItemAtPosition(position);
                amount = Long.parseLong(spinnerItems.getAmount());

//                Toast.makeText(getContext(), amount + " selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        // get coins from database
        database.collection("User")
                .document(auth.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user = documentSnapshot.toObject(User.class); // convert that all snapshot to user model structure object.
                        binding.currentCoins.setText(String.valueOf(user.getCoins())); // set coins
                        binding.userName.setText(user.getUserName()); // set name
                    }
                });

        // send request to database with spin coins
        binding.sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasCheckedInToday = checkPreferences.getBoolean("hasCheckedInToday", false);
                if (hasCheckedInToday){
                    if (user.getCoins() >= 1000){
                        String uid = FirebaseAuth.getInstance().getUid();
                        String email = binding.emailRequest.getText().toString();
                        String userName = user.getUserName();

                        WithdrawRequest request = new WithdrawRequest(uid, email, userName, amount);

                        if (!email.isEmpty()){
                            database.collection("withdraws")
                                    .document(uid)
                                    .set(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // if i withdraw coins then, update or minus coins here.
                                            long updatedCoins = (user.getCoins() - amount * 100); // (10 = 1000) (20 = 2000)
                                            database.collection("User")
                                                    .document(uid)
                                                    .update("coins", updatedCoins);

                                            binding.emailRequest.getText().clear(); // clearing email of edittext.
                                            Toast.makeText(getContext(), "Request sent Successfully", Toast.LENGTH_LONG).show();

                                            // shift to home fragment for update coins.
                                            goToHomeFragment();
                                        }
                                    });
                        } else {
                            Toast.makeText(getContext(), "Provide Email/Number", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "You Don't Have Enough Coins!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "You Haven't Daily Checked Yet.", Toast.LENGTH_LONG).show();
                }

            }
        });

        // Inflate the layout for this fragment
        return binding.getRoot();

    }

    private void goToHomeFragment() {
        // go one fragment to another fragment.
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content, new HomeFragment());
        ft.addToBackStack(null);
        ft.commit();
    }



    private void spinnerItemList(){
        // fill arraylist with items
        spinnerList = new ArrayList<>();

        spinnerList.add(new spinnerItems("10", R.drawable.rupee));
        spinnerList.add(new spinnerItems("20", R.drawable.rupee));
//        spinnerList.add(new spinnerItems("50", R.drawable.dollar));
//        spinnerList.add(new spinnerItems("100", R.drawable.dollar));
//        spinnerList.add(new spinnerItems("500", R.drawable.dollar));
//        spinnerList.add(new spinnerItems("1000", R.drawable.dollar));
    }



}