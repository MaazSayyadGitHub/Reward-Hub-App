package com.maaz.rewardhub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class categoryAdapter extends RecyclerView.Adapter<categoryAdapter.viewHolder> {

    Context context;
    ArrayList<categoryModel> categoryModels;

    public categoryAdapter(Context context, ArrayList<categoryModel> categoryModels){
        this.context = context;
        this.categoryModels = categoryModels;
    }

    @NonNull
    @Override
    public categoryAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_items, parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull categoryAdapter.viewHolder holder, int position) {

        categoryModel model = categoryModels.get(position); // we got positions now.

        // set text for category
        holder.textView.setText(model.getCategoryName()); // here it will set categoryName.

        // set imageView for category
        Glide.with(context)
                .load(model.getCategoryImage())
                .into(holder.imageView);

        // opening new Activities for each category.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (model.getCategoryName().equalsIgnoreCase("Daily CheckIn")){ // Open CheckIn Activity
                    Intent intent = new Intent(context, DailyCheckIn.class);
                    context.startActivity(intent);
                }
                else if (model.getCategoryName().equalsIgnoreCase("Watch & Earn")){ // Open Watch & Earn
                    Intent intent = new Intent(context, WatchAndEarn.class);
                    context.startActivity(intent);
                }
                else if (model.getCategoryName().equalsIgnoreCase("Recaptcha")){ // Recaptcha
                    Intent intent = new Intent(context, Recaptcha.class);
                    context.startActivity(intent);
                }
                else { // Scratch & Earn
                    Intent intent = new Intent(context, ScratchAndEarn.class);
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.category);
        }
    }
}
