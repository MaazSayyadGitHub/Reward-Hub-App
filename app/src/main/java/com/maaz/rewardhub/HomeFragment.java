package com.maaz.rewardhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.maaz.rewardhub.databinding.FragmentHomeBinding;

import java.util.ArrayList;


public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    FragmentHomeBinding binding;
    FirebaseFirestore database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        database = FirebaseFirestore.getInstance();

        // arrayList for categories.
        ArrayList<categoryModel> categories = new ArrayList<>();
        // object of adapter class
        categoryAdapter adapter = new categoryAdapter(getContext(), categories);

        // fetching categories from firestore.
        // we are fetching all documents so we are directly taking snapShot after collection.
        // snapshot listener is use for real time data update.(if in firebase any data will update this snapshot
        // will update in realtime).
        database.collection("categories")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        categories.clear(); // whenever we update previous list will be clear.

                        for (DocumentSnapshot snapshot : value.getDocuments()) { // document type data in snapshot.
                            categoryModel model = snapshot.toObject(categoryModel.class); // putting in categoryModel.
                            model.setCategoryId(snapshot.getId()); // getting each database document id.
                            categories.add(model); // adding in arrayList.
                        }
                        // update data in recyclerview
                        adapter.notifyDataSetChanged();
                    }
                });

        // set LayoutManager of RecyclerView
        binding.categoryList.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        // set adapter on recyclerView.
        // using binding for ids (categoryList is recyclerView id).
        binding.categoryList.setAdapter(adapter);

        // set spinWheel button
        binding.spinWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SpinnerActivity.class));
            }
        });

        binding.games.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telegramUrl = "https://t.me/branddreamgamer";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(telegramUrl));
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}