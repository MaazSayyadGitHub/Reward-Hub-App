package com.maaz.rewardhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<spinnerItems> {

    public SpinnerAdapter(Context context, ArrayList<spinnerItems> spinnerList){
        super(context, 0, spinnerList); // we are passing directly to super arrayAdapter Class.
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }


    private View initView(int position, View convertView, ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_layout, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.image);
        TextView amount = convertView.findViewById(R.id.amount);

        spinnerItems spinnerItems = getItem(position);

        image.setImageResource(spinnerItems.getImage());
        amount.setText(spinnerItems.getAmount());

        return convertView;

    }
}
