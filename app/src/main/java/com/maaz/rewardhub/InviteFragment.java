package com.maaz.rewardhub;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maaz.rewardhub.databinding.FragmentInviteBinding;


public class InviteFragment extends Fragment {



    public InviteFragment() {
        // Required empty public constructor
    }

    FragmentInviteBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInviteBinding.inflate(inflater, container, false);

        binding.shareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPackageName = "com.maaz.rewardhub";

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                    startActivity(intent);
                } catch (ActivityNotFoundException e){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                    startActivity(intent);
                }

            }
        });

        return binding.getRoot();
    }
}